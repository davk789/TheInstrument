
SampleLooper {
	var win, sampleView, markerBar, zoomSlider;
	*new {
		^super.new.init_samplelooper;	
	}
	init_samplelooper {
		this.initGUI;
	}
	initGUI {
		var winWidth, sampleViewRow, vZoomSlider;
		win = GUI.window.new("Sample Looper", Rect.new(50, 500, 600, 200))
			.front;
		win.view.background = Color.black;
		win.view.decorator = FlowLayout(win.view.bounds);
		winWidth = win.view.bounds.width - 45;
		markerBar = MarkerBar.new(win, Rect.new(0, 0, winWidth, 20))
			.markerColor_(Color.yellow)
			.background_(Color.white.alpha_(0.3));
		sampleViewRow = GUI.hLayoutView.new(win, Rect.new(0, 0, winWidth + 45, 100));
		sampleView = GUI.soundFileView.new(sampleViewRow, Rect.new(0, 0, winWidth, 0))
			.background_(Color.white.alpha_(0.3));
		vZoomSlider = GUI.vLayoutView.new(sampleViewRow, Rect.new(0, 0, 35, 0));
		GUI.slider.new(vZoomSlider, Rect.new(0, 0, 0, 100));
		zoomSlider = GUI.rangeSlider.new(win, Rect.new(0, 0, winWidth, 20))
			.knobColor_(Color.new255(109, 126, 143))
			.background_(Color.white.alpha_(0.3));
	}
}


       