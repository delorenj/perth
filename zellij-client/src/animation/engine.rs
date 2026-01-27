// Perth STORY-004: AnimationEngine trait and core types
use std::time::{Duration, Instant};

/// Defines a rectangular region that needs to be redrawn
#[derive(Debug, Clone, PartialEq, Eq)]
pub struct DirtyRegion {
    pub x: usize,
    pub y: usize,
    pub width: usize,
    pub height: usize,
}

/// Represents a single frame of animation with dirty regions
#[derive(Debug, Clone)]
pub struct AnimationFrame {
    /// The rendered content for this frame
    pub content: String,
    /// Regions that changed from the previous frame
    pub dirty_regions: Vec<DirtyRegion>,
    /// Frame timestamp for FPS calculation
    pub timestamp: Instant,
}

impl AnimationFrame {
    pub fn new(content: String, dirty_regions: Vec<DirtyRegion>) -> Self {
        Self {
            content,
            dirty_regions,
            timestamp: Instant::now(),
        }
    }
}

/// Frame-based animation interface
pub trait AnimationEngine: Send + Sync {
    /// Generate the next frame of animation
    /// Returns None if animation is complete (for finite animations)
    fn next_frame(&mut self) -> Option<AnimationFrame>;

    /// Get the target FPS for this animation
    fn target_fps(&self) -> u32;

    /// Get the frame duration based on target FPS
    fn frame_duration(&self) -> Duration {
        Duration::from_secs_f64(1.0 / self.target_fps() as f64)
    }

    /// Reset animation to initial state
    fn reset(&mut self);

    /// Check if animation should degrade to lower FPS based on CPU usage
    /// Default implementation always returns target FPS
    fn adaptive_fps(&self, _cpu_usage_percent: f32) -> u32 {
        let target = self.target_fps();
        // Graceful degradation: drop to 30fps if CPU >80%
        if _cpu_usage_percent > 80.0 && target > 30 {
            30
        } else {
            target
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_dirty_region_equality() {
        let region1 = DirtyRegion { x: 0, y: 0, width: 10, height: 1 };
        let region2 = DirtyRegion { x: 0, y: 0, width: 10, height: 1 };
        assert_eq!(region1, region2);
    }

    #[test]
    fn test_frame_duration_60fps() {
        struct MockAnimation;
        impl AnimationEngine for MockAnimation {
            fn next_frame(&mut self) -> Option<AnimationFrame> { None }
            fn target_fps(&self) -> u32 { 60 }
            fn reset(&mut self) {}
        }

        let anim = MockAnimation;
        let duration = anim.frame_duration();
        // 60fps = ~16.67ms per frame
        assert!(duration.as_millis() >= 16 && duration.as_millis() <= 17);
    }

    #[test]
    fn test_adaptive_fps_degradation() {
        struct MockAnimation;
        impl AnimationEngine for MockAnimation {
            fn next_frame(&mut self) -> Option<AnimationFrame> { None }
            fn target_fps(&self) -> u32 { 60 }
            fn reset(&mut self) {}
        }

        let anim = MockAnimation;
        // Normal CPU usage - maintain 60fps
        assert_eq!(anim.adaptive_fps(50.0), 60);
        // High CPU usage - degrade to 30fps
        assert_eq!(anim.adaptive_fps(85.0), 30);
    }
}
