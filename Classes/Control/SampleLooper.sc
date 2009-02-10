
SampleLooper {
	var win;
	*new {
		^super.new.init_samplelooper;	
	}
	init_samplelooper {
		this.initGUI;	
	}
	initGUI {
		win = GUI.window.new("Sample Looper", Rect.new(50, 500, 300, 200)).front;
		win.view.decorator = FlowLayout(win.view.bounds);
		GUI.staticText.new(win, win.view.bounds)
			.align_('center')
			.string_("there should be a GUI.soundFileView object here.");
	}
}


       