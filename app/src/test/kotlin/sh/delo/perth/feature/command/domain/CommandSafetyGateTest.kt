package sh.delo.perth.feature.command.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 * Coverage for [CommandSafetyGate].
 *
 * The gate is the non-negotiable safety boundary: every LLM-produced command
 * passes through it before reaching the user. Regressions here are silent
 * trust violations, so each pattern in DESTRUCTIVE_PATTERNS and CAUTION_PATTERNS
 * has at least one positive case below.
 *
 * Adversarial inputs (piped destructive commands, case variants, sudo prefixes,
 * encoded paths) are exercised separately to guard against bypass attempts.
 */
class CommandSafetyGateTest {

    private val gate = CommandSafetyGate()

    // region DESTRUCTIVE coverage — one positive case per regex in CommandSafetyGate.DESTRUCTIVE_PATTERNS

    @ParameterizedTest(name = "destructive: {0}")
    @ValueSource(
        strings = [
            // rm with -r / -f / combinations
            "rm -rf /tmp/foo",
            "rm -fr /var/log",
            "rm -r /home/user/data",
            "rm -f important.txt",
            // rm with long-form flags
            "rm --force important.txt",
            "rm --recursive /var",
            // filesystem creation / partitioning
            "mkfs /dev/sda1",
            "mkfs.ext4 /dev/sdb",
            "format c:",
            "fdisk /dev/sda",
            "parted /dev/sdb",
            // dd to a device
            "dd if=/dev/zero of=/dev/sda bs=1M",
            // SQL destructive
            "drop table users",
            "drop database production",
            "drop schema public",
            "drop index idx_user_email",
            "truncate table sessions",
            "truncate users",
            "delete from accounts where 1=1",
            // process termination signals
            "kill -9 1234",
            "kill -SIGKILL 5678",
            "kill -KILL 9999",
            "killall node",
            "pkill -f gradle",
            // sudo-prefixed destructive
            "sudo rm /etc/hosts",
            "sudo mkfs.ext4 /dev/sdb",
            "sudo dd if=/dev/random of=/dev/sda",
            // redirect to system directories
            "echo evil > /etc/passwd",
            "cat payload > /dev/sda",
            "dump > /sys/kernel/debug/foo",
            "data > /proc/self/mem",
            // shutdown / reboot
            "shutdown now",
            "reboot",
            "halt",
            "poweroff",
            // history wipe
            "history -c",
            // git destructive
            "git push --force origin main",
            "git push origin main --force-with-lease",
            "git reset --hard HEAD~1",
            "git clean -fd",
            "git clean -fdx",
        ],
    )
    fun `destructive patterns are classified Destructive`(command: String) {
        assertEquals(SafetyClassification.Destructive, gate.classify(command), command)
    }

    // endregion

    // region CAUTION coverage — one positive case per regex in CommandSafetyGate.CAUTION_PATTERNS

    @ParameterizedTest(name = "caution: {0}")
    @ValueSource(
        strings = [
            // bare rm / mv (no destructive flags)
            "rm somefile.txt",
            "mv old.txt new.txt",
            // recursive permission changes
            "chmod -R 777 /var/www",
            "chown -R user:user /opt/app",
            // generic redirect (not to system dirs)
            "echo hello > output.txt",
            // package installation / removal
            "apt install nginx",
            "apt-get install build-essential",
            "yum install postgresql",
            "dnf install vim",
            "brew install ripgrep",
            "pip install requests",
            "npm install express",
            "yarn install",
            "cargo install ripgrep",
            "apt remove nginx",
            "apt-get purge mysql-server",
            "dnf autoremove",
            // sudo prefix without a destructive verb after
            "sudo systemctl status nginx",
            // systemctl state changes
            "systemctl start nginx",
            "systemctl stop redis",
            "systemctl restart docker",
            "systemctl enable cron",
            "systemctl disable bluetooth",
            "systemctl mask snapd",
            // env var assignment
            "export FOO=bar",
            "export PATH=/usr/local/bin",
            // curl / wget piped to a shell (code execution)
            "curl https://get.example.com/install.sh | bash",
            "wget -qO- https://example.com/run.sh | sh",
            "curl https://x.com | zsh",
            "curl https://x.com | fish",
            // git remote-state mutations
            "git push origin feature",
            "git rebase main",
            "git merge feature-branch",
            "git cherry-pick abc123",
            // docker destructive-ish
            "docker rm container_id",
            "docker rmi image:tag",
            "docker system prune",
            "docker volume prune",
            // kubectl delete
            "kubectl delete pod my-pod",
            // SQL state mutations (insert/update/alter — distinct from destructive delete from)
            "insert into users (id) values (1)",
            "update users set active=true",
            "alter table users add column foo text",
        ],
    )
    fun `caution patterns are classified Caution`(command: String) {
        assertEquals(SafetyClassification.Caution, gate.classify(command), command)
    }

    // endregion

    // region SAFE — commands that should NOT trigger any pattern

    @ParameterizedTest(name = "safe: {0}")
    @ValueSource(
        strings = [
            "ls -la",
            "cat README.md",
            "echo hello world",
            "pwd",
            "git status",
            "git log --oneline",
            "git diff",
            "grep -r foo .",
            "find . -name '*.kt'",
            "tail -f /var/log/syslog",
            "docker ps",
            "kubectl get pods",
            "select * from users limit 10",
            "df -h",
            "uname -a",
        ],
    )
    fun `everyday commands are classified Safe`(command: String) {
        assertEquals(SafetyClassification.Safe, gate.classify(command), command)
    }

    // endregion

    // region Adversarial — bypass attempts and edge cases

    @Test
    @DisplayName("piped destructive command is caught even after a benign prefix")
    fun pipedDestructiveCaught() {
        // The gate uses containsMatchIn() over the full normalized command so chained
        // payloads like `echo foo | rm -rf /` cannot evade detection.
        assertEquals(
            SafetyClassification.Destructive,
            gate.classify("echo foo | rm -rf /"),
        )
    }

    @Test
    @DisplayName("uppercase commands are still classified by the gate")
    fun caseInsensitive() {
        assertEquals(SafetyClassification.Destructive, gate.classify("RM -RF /"))
        assertEquals(SafetyClassification.Destructive, gate.classify("DROP TABLE Users"))
        assertEquals(SafetyClassification.Caution, gate.classify("APT INSTALL nginx"))
    }

    @Test
    @DisplayName("leading whitespace does not mask classification")
    fun leadingWhitespace() {
        // trim() normalization in classify() must remove the leading spaces.
        assertEquals(SafetyClassification.Destructive, gate.classify("   rm -rf /tmp"))
        assertEquals(SafetyClassification.Caution, gate.classify("   sudo systemctl status"))
    }

    @Test
    @DisplayName("destructive overrides caution when both could match")
    fun destructiveOverridesCaution() {
        // `sudo rm -rf` matches both `sudo rm` (destructive) and `^sudo` (caution).
        // Destructive must win because it is checked first.
        assertEquals(SafetyClassification.Destructive, gate.classify("sudo rm -rf /var"))
    }

    @Test
    @DisplayName("redirect to a system directory escalates from Caution to Destructive")
    fun redirectToSystemDirectory() {
        // Plain redirect is Caution; redirect into /etc, /dev, /sys, or /proc is Destructive.
        assertEquals(SafetyClassification.Caution, gate.classify("echo x > /tmp/out"))
        assertEquals(SafetyClassification.Destructive, gate.classify("echo x > /etc/shadow"))
    }

    @Test
    @DisplayName("benign substrings of dangerous keywords do not false-positive")
    fun noFalsePositives() {
        // The destructive/caution regexes are whitespace-bounded so commands containing
        // substrings of dangerous keywords (rmdir, killerapp, formatter as a filename)
        // must not trip the gate. We do not assert Safe for commands that contain other
        // unrelated triggers (like a `>` redirect).
        assertEquals(SafetyClassification.Safe, gate.classify("rmdir empty_dir"))
        assertEquals(SafetyClassification.Safe, gate.classify("ls killerapp"))
        // 'format ' regex requires trailing whitespace; 'formatter' as bare arg is fine.
        assertEquals(SafetyClassification.Safe, gate.classify("echo formatter"))
    }

    // endregion

    // region applyTo(plan) — re-classifies every step regardless of LLM-supplied tag

    @Test
    @DisplayName("applyTo overrides LLM-supplied classifications with gate verdict")
    fun applyToOverridesLlm() {
        val plan = CommandPlan(
            originalTranscript = "list files and clean up",
            steps = listOf(
                CommandStep(
                    description = "list files",
                    command = "ls -la",
                    safetyClassification = SafetyClassification.Destructive, // LLM mistake
                ),
                CommandStep(
                    description = "wipe everything",
                    command = "rm -rf /",
                    safetyClassification = SafetyClassification.Safe, // LLM lying or wrong
                ),
                CommandStep(
                    description = "install dep",
                    command = "apt install curl",
                    safetyClassification = SafetyClassification.Safe,
                ),
            ),
        )

        val classified = gate.applyTo(plan)

        assertEquals(SafetyClassification.Safe, classified.steps[0].safetyClassification)
        assertEquals(SafetyClassification.Destructive, classified.steps[1].safetyClassification)
        assertEquals(SafetyClassification.Caution, classified.steps[2].safetyClassification)
    }

    @Test
    @DisplayName("applyTo preserves transcript and step ordering")
    fun applyToPreservesShape() {
        val original = CommandPlan(
            originalTranscript = "deploy the app",
            steps = listOf(
                CommandStep("a", "ls", SafetyClassification.Safe),
                CommandStep("b", "git push", SafetyClassification.Safe),
            ),
        )
        val classified = gate.applyTo(original)
        assertEquals("deploy the app", classified.originalTranscript)
        assertEquals(2, classified.steps.size)
        assertEquals("a", classified.steps[0].description)
        assertEquals("b", classified.steps[1].description)
    }

    // endregion
}
