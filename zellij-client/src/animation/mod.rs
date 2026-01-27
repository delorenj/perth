// Perth STORY-004: Animation Engine
// Frame-based animation system for visual indicators

pub mod engine;
pub mod candycane;

pub use engine::{AnimationEngine, AnimationFrame, DirtyRegion};
pub use candycane::CandycaneAnimation;
