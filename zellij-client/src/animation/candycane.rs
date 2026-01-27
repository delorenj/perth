// Perth STORY-004: Candycane animation pattern
// █▓▒░░▒▓█ repeating pattern that shifts 1 cell/frame at 60fps

use super::engine::{AnimationEngine, AnimationFrame, DirtyRegion};

/// Candycane animation: horizontal bar with shifting gradient pattern
/// Pattern: █▓▒░░▒▓█ (dark to light to dark, symmetric)
pub struct CandycaneAnimation {
    /// Width of the animation bar in characters
    width: usize,
    /// Current frame number (used for pattern shifting)
    frame_count: usize,
    /// Target frames per second
    fps: u32,
    /// Y-coordinate of the animation bar (for dirty region calculation)
    y_position: usize,
    /// X-coordinate offset (for dirty region calculation)
    x_offset: usize,
}

impl CandycaneAnimation {
    /// The candycane pattern characters, from darkest to lightest
    const PATTERN: [char; 4] = ['█', '▓', '▒', '░'];

    /// Create a new candycane animation
    pub fn new(width: usize, y_position: usize, x_offset: usize) -> Self {
        Self {
            width,
            frame_count: 0,
            fps: 60,
            y_position,
            x_offset,
        }
    }

    /// Set custom FPS (for testing or performance tuning)
    pub fn with_fps(mut self, fps: u32) -> Self {
        self.fps = fps;
        self
    }

    /// Generate the pattern for the current frame
    /// Pattern shifts left by 1 cell each frame (creating rightward motion illusion)
    fn generate_pattern(&self) -> String {
        let pattern_len = Self::PATTERN.len();
        let mut result = String::with_capacity(self.width);

        for i in 0..self.width {
            // Calculate position in the pattern, shifted by frame_count
            // We shift left (subtract frame_count) to create rightward motion
            let pattern_index = (i + self.frame_count) % pattern_len;
            result.push(Self::PATTERN[pattern_index]);
        }

        result
    }
}

impl AnimationEngine for CandycaneAnimation {
    fn next_frame(&mut self) -> Option<AnimationFrame> {
        let content = self.generate_pattern();

        // Only the horizontal bar region is dirty (optimization for rendering)
        let dirty_region = DirtyRegion {
            x: self.x_offset,
            y: self.y_position,
            width: self.width,
            height: 1, // Horizontal bar is 1 character tall
        };

        self.frame_count += 1;

        Some(AnimationFrame::new(content, vec![dirty_region]))
    }

    fn target_fps(&self) -> u32 {
        self.fps
    }

    fn reset(&mut self) {
        self.frame_count = 0;
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_pattern_generation() {
        let mut anim = CandycaneAnimation::new(8, 0, 0);

        // Frame 0: Pattern starts at beginning
        let frame0 = anim.next_frame().unwrap();
        assert_eq!(frame0.content, "█▓▒░█▓▒░");

        // Frame 1: Pattern shifts left by 1 (appears to move right)
        let frame1 = anim.next_frame().unwrap();
        assert_eq!(frame1.content, "▓▒░█▓▒░█");

        // Frame 2: Pattern continues shifting
        let frame2 = anim.next_frame().unwrap();
        assert_eq!(frame2.content, "▒░█▓▒░█▓");

        // Frame 3: Pattern continues
        let frame3 = anim.next_frame().unwrap();
        assert_eq!(frame3.content, "░█▓▒░█▓▒");

        // Frame 4: Pattern wraps around (4 % 4 = 0, back to start)
        let frame4 = anim.next_frame().unwrap();
        assert_eq!(frame4.content, "█▓▒░█▓▒░");
    }

    #[test]
    fn test_pattern_width() {
        let mut anim = CandycaneAnimation::new(4, 0, 0);
        let frame = anim.next_frame().unwrap();
        // Unicode characters are multi-byte, so check character count not byte length
        assert_eq!(frame.content.chars().count(), 4);
        assert_eq!(frame.content, "█▓▒░");
    }

    #[test]
    fn test_dirty_region() {
        let mut anim = CandycaneAnimation::new(10, 5, 2);
        let frame = anim.next_frame().unwrap();

        assert_eq!(frame.dirty_regions.len(), 1);
        let region = &frame.dirty_regions[0];
        assert_eq!(region.x, 2); // x_offset
        assert_eq!(region.y, 5); // y_position
        assert_eq!(region.width, 10); // animation width
        assert_eq!(region.height, 1); // horizontal bar
    }

    #[test]
    fn test_reset() {
        let mut anim = CandycaneAnimation::new(8, 0, 0);

        // Advance a few frames
        anim.next_frame();
        anim.next_frame();
        anim.next_frame();

        // Reset should go back to frame 0
        anim.reset();
        let frame = anim.next_frame().unwrap();
        assert_eq!(frame.content, "█▓▒░█▓▒░");
    }

    #[test]
    fn test_target_fps() {
        let anim = CandycaneAnimation::new(8, 0, 0);
        assert_eq!(anim.target_fps(), 60);

        let custom_anim = CandycaneAnimation::new(8, 0, 0).with_fps(30);
        assert_eq!(custom_anim.target_fps(), 30);
    }

    #[test]
    fn test_frame_duration() {
        let anim = CandycaneAnimation::new(8, 0, 0);
        let duration = anim.frame_duration();
        // 60fps = ~16.67ms per frame
        assert!(duration.as_millis() >= 16 && duration.as_millis() <= 17);
    }

    #[test]
    fn test_pattern_continuity() {
        // Verify pattern cycles correctly over multiple frames
        let mut anim = CandycaneAnimation::new(4, 0, 0);

        let frames: Vec<String> = (0..8)
            .map(|_| anim.next_frame().unwrap().content)
            .collect();

        // After 4 frames, pattern should repeat (pattern length is 4)
        assert_eq!(frames[0], frames[4]);
        assert_eq!(frames[1], frames[5]);
        assert_eq!(frames[2], frames[6]);
        assert_eq!(frames[3], frames[7]);
    }

    #[test]
    fn test_infinite_animation() {
        // Candycane animation is infinite - next_frame() always returns Some
        let mut anim = CandycaneAnimation::new(8, 0, 0);

        for _ in 0..1000 {
            assert!(anim.next_frame().is_some());
        }
    }
}
