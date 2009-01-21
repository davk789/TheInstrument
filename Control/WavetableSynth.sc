WavetableSynth {
	var buffer0=70, buffer1=71, s,
	win, pen, sineSummer, sineParams;
	*new {
		^super.new.init_wvsynth;
	}
	init_wvsynth {
	    s = Server.default;
		s.sendMsg('b_gen', 70, 'sine1', 5, 1);
		s.sendMsg('b_gen', 71, 'sine1', 5, 1);
		this.initGUI;
	}
	initGUI {
		win = GUI.window.new("|7|", Rect.new(200.rand, 400.rand, 385, 505));
		pen = GUI.pen.new;

		sineSummer = GUI.compositeView.new(win, Rect.new(0,0,380,500));
		sineSummer.decorator = FlowLayout(sineSummer.bounds);
		sineSummer.background_(Color.new255(220, 230, 240, 180));

		sineParams = GUI.compositeView.new(sineSummer, Rect.new(0,0,372,65));
		sineParams.decorator = FlowLayout(sineSummer.bounds);
		sineParams.background_(Color.new255(240, 220, 230, 125));
        
        win.front;

	}
}

