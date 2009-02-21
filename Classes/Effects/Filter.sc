LowpassFilter {
	var win;
	*new {
		^super.new.init_lowpassfilter;
	}
	init_lowpassfilter {
		this.initGUI;
		
	}
	initGUI {
		win = GUI.window.new("LowPassFilter", Rect.new(100, 300, 500, 300))
			.background_(Color.black)
			.front;
		win.view.decorator = FlowLayout(win.view.bounds);
		
		
	}
}