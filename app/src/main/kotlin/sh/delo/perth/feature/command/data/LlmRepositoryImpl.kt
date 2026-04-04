package sh.delo.perth.feature.command.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import sh.delo.perth.core.data.secure.SecureStorage
import sh.delo.perth.core.domain.repository.LlmRepository
import sh.delo.perth.core.result.AppException
import sh.delo.perth.core.result.AppResult
import sh.delo.perth.core.result.runCatchingAppResult
import sh.delo.perth.feature.command.domain.CommandPlan
import sh.delo.perth.feature.command.domain.CommandStep
import sh.delo.perth.feature.command.domain.SafetyClassification
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LlmRepositoryImpl @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val secureStorage: SecureStorage,
) : LlmRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // ---------------------------------------------------------------------------
    // LlmRepository implementation
    // ---------------------------------------------------------------------------

    override suspend fun interpretCommand(transcript: String): AppResult<String> =
        interpretCommandPlan(transcript).map { plan ->
            plan.steps.joinToString(" && ") { it.command }
        }

    override suspend fun interpretCommandPlan(transcript: String): AppResult<CommandPlan> =
        withContext(Dispatchers.IO) {
            val apiKey = secureStorage.getString(SecureStorage.KEY_LLM_API_KEY)
                ?: return@withContext AppResult.Error(
                    AppException.Command("OpenAI API key is not configured. Add it in Settings."),
                )

            runCatchingAppResult(
                errorMapper = { e -> AppException.Command("LLM request failed: ${e.message}", e) },
            ) {
                val requestBody = buildChatRequest(
                    messages = listOf(
                        ChatMessage(role = "system", content = SYSTEM_PROMPT),
                        ChatMessage(role = "user", content = transcript),
                    ),
                )

                val response = executeRequest(apiKey, requestBody)
                val planJson = extractContent(response)
                parsePlan(transcript, planJson)
            }
        }

    override suspend fun validateApiKey(): AppResult<Unit> =
        withContext(Dispatchers.IO) {
            val apiKey = secureStorage.getString(SecureStorage.KEY_LLM_API_KEY)
                ?: return@withContext AppResult.Error(
                    AppException.Command("No API key configured."),
                )

            runCatchingAppResult(
                errorMapper = { e -> AppException.Command("API key validation failed: ${e.message}", e) },
            ) {
                // Lightweight probe: list models endpoint requires a valid key
                val request = Request.Builder()
                    .url("$BASE_URL/models")
                    .get()
                    .header("Authorization", "Bearer $apiKey")
                    .build()

                val response = okHttpClient.newCall(request).execute()
                response.use {
                    if (!it.isSuccessful) {
                        val code = it.code
                        val body = it.body?.string() ?: ""
                        Timber.w("validateApiKey: HTTP %d body=%s", code, body)
                        throw AppException.Server(code, "API key validation failed (HTTP $code)")
                    }
                }
                Unit
            }
        }

    override suspend fun classifyRisk(command: String): AppResult<LlmRepository.RiskLevel> =
        AppResult.Success(LlmRepository.RiskLevel.Moderate) // Delegated to CommandSafetyGate in domain

    // ---------------------------------------------------------------------------
    // HTTP helpers
    // ---------------------------------------------------------------------------

    private fun buildChatRequest(messages: List<ChatMessage>): String {
        val messagesJson = messages.joinToString(",\n") { msg ->
            // Escape content for JSON embedding
            val escapedContent = msg.content
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
            """{"role":"${msg.role}","content":"$escapedContent"}"""
        }
        return """{"model":"$MODEL","response_format":{"type":"json_object"},"messages":[$messagesJson]}"""
    }

    private fun executeRequest(apiKey: String, bodyJson: String): ChatCompletionResponse {
        val body = bodyJson.toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url("$BASE_URL/chat/completions")
            .post(body)
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .build()

        val response = okHttpClient.newCall(request).execute()
        response.use { resp ->
            if (!resp.isSuccessful) {
                val code = resp.code
                val errorBody = resp.body?.string() ?: ""
                Timber.w("LLM request failed HTTP %d: %s", code, errorBody)
                throw AppException.Server(code, "OpenAI returned HTTP $code")
            }
            val responseBody = resp.body?.string()
                ?: throw AppException.Command("Empty response body from OpenAI")
            return json.decodeFromString(ChatCompletionResponse.serializer(), responseBody)
        }
    }

    private fun extractContent(response: ChatCompletionResponse): String {
        return response.choices.firstOrNull()?.message?.content
            ?: throw AppException.Command("No content in LLM response")
    }

    private fun parsePlan(originalTranscript: String, planJson: String): CommandPlan {
        val raw = try {
            json.decodeFromString(RawPlanResponse.serializer(), planJson)
        } catch (e: Exception) {
            Timber.e(e, "Failed to parse LLM JSON response: %s", planJson)
            throw AppException.Command("Could not parse command plan from LLM response.", e)
        }

        if (raw.steps.isEmpty()) {
            throw AppException.Command(
                "Could not determine a command. Try rephrasing.",
            )
        }

        val steps = raw.steps.map { rawStep ->
            CommandStep(
                description = rawStep.description,
                command = rawStep.command,
                // Initial classification from LLM; CommandSafetyGate overrides this
                safetyClassification = when (rawStep.riskLevel.lowercase()) {
                    "destructive" -> SafetyClassification.Destructive
                    "caution" -> SafetyClassification.Caution
                    else -> SafetyClassification.Safe
                },
                isApproved = false,
            )
        }

        return CommandPlan(originalTranscript = originalTranscript, steps = steps)
    }

    // ---------------------------------------------------------------------------
    // Serialization models (internal)
    // ---------------------------------------------------------------------------

    @Serializable
    private data class ChatMessage(
        val role: String,
        val content: String,
    )

    @Serializable
    private data class ChatCompletionResponse(
        val choices: List<Choice>,
    )

    @Serializable
    private data class Choice(
        val message: ChoiceMessage,
    )

    @Serializable
    private data class ChoiceMessage(
        val content: String,
    )

    @Serializable
    private data class RawPlanResponse(
        val steps: List<RawStep> = emptyList(),
    )

    @Serializable
    private data class RawStep(
        val description: String,
        val command: String,
        @SerialName("riskLevel") val riskLevel: String = "safe",
    )

    companion object {
        private const val BASE_URL = "https://api.openai.com/v1"
        private const val MODEL = "gpt-4o-mini"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        /**
         * System prompt instructing the model to return a structured JSON plan.
         * The response_format=json_object mode enforces valid JSON output.
         */
        private const val SYSTEM_PROMPT = """You are a terminal command assistant for a Linux/Zellij environment.
The user will describe what they want to accomplish in natural language.
Return ONLY a JSON object with this exact structure:
{
  "steps": [
    {
      "description": "Human-readable description of what this step does",
      "command": "the exact shell command to run",
      "riskLevel": "safe" | "caution" | "destructive"
    }
  ]
}

Rules:
- riskLevel must be one of: "safe", "caution", "destructive"
- "destructive" = commands that delete, overwrite, or irreversibly modify data (rm -rf, DROP TABLE, mkfs, dd, kill -9, etc.)
- "caution" = commands with significant side-effects (sudo, mv, install, chmod -R, git push, docker rm, etc.)
- "safe" = read-only or benign commands (ls, cat, git status, echo, pwd, etc.)
- If you cannot determine a safe command sequence for the request, return {"steps": []}
- Keep commands minimal and precise. Prefer idempotent operations where possible.
- Do not include explanatory text outside the JSON object."""
    }
}

