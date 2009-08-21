SynthBase {
	var win, winBounds;
	*new {
		^super.new.init_synthbase;
	}
	
	init_synthBase {
		winBounds = Rect.new(500.rand, 500.rand, 300, 300);
	}
	
	makeWindow {
		
	}
	
}