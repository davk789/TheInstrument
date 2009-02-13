
SampleLooper {
	var win, sampleView;
	*new {
		^super.new.init_samplelooper;	
	}
	init_samplelooper {
		this.initGUI;	
	}
	initGUI {
		var winWidth;
		win = GUI.window.new("Sample Looper", Rect.new(50, 500, 600, 200)).front;
		win.view.decorator = FlowLayout(win.view.bounds);
		winWidth = win.view.bounds.width;
		sampleView = GUI.soundFileView.new(win, Rect.new(0, 0, winWidth, 100));
	}
}


       