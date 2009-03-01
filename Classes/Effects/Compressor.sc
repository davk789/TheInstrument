Compressor : EffectBase {
	var	bus=20, controlBus=20, mix=1, 
		threshold=0.6, slopeBelow=1, slopeAbove=0.2, 
		clampTime=0.005, relaxTime=0.01;
	*new { |group, name, ind|
		^super.new(group, name, ind).init_compressor;
	}
	init_compressor {
		this.updateStartParams;
		this.startSynth;
		this.initGUI;
	}
	updateStartParams {
		startParams = ['threshold', threshold, 'slopeBelow', slopeBelow, 'slopeAbove', slopeAbove, 
			'clampTime', clampTime, 'relaxTime', relaxTime];
	}
	setThreshold { |val|
		threshold = val;
		server.sendMsg('n_set', groupID, 'threshold', threshold);
	}
	setSlopeBelow { |val|
		slopeBelow = val;
		server.sendMsg('n_set', groupID, 'slopeBelow', slopeBelow);
	}
	setSlopeAbove { |val|
		slopeAbove = val;
		server.sendMsg('n_set', groupID, 'slopeAbove', slopeAbove);
	}
	setClampTime { |val|
		clampTime = val;
		server.sendMsg('n_set', groupID, 'clampTime', clampTime);
	}
	setRelaxTime { |val|
		relaxTime = val;
		server.sendMsg('n_set', groupID, 'relaxTime', relaxTime);
	}
	makeGUI {
		var fullWidth, controlRow, knobColors, controlMenu;
		~audioBusRegister.postln;
		knobColors = [Color.white.alpha_(0.5), Color.yellow, Color.black, Color.yellow];
		if(win.isClosed){ 
			win = nil;
			this.initGUI;
		};
		fullWidth = win.view.bounds.width - 10;
		GUI.staticText.new(win, Rect.new(0, 0, 63, 20))
			.stringColor_(Color.yellow)
			.string_("sidechain: ")
			.align_('right');
		controlMenu = GUI.popUpMenu.new(win, Rect.new(0, 0, 130, 20))
			.background_(Color.clear)
			.stringColor_(Color.yellow)
			.items_(~audioBusRegister.keys.asArray);
		controlMenu.value = controlMenu.items.indexOf(~audioBusRegister.findKeyForValue(controlBus));
		GUI.button.new(win, Rect.new(0, 0, 40, 20))
			.states_([
				["bypass", Color.black, Color.white(0.6).alpha_(0.6)],
				["bypass", Color.black, Color.yellow]
			]);
		GUI.staticText.new(win, Rect.new(0, 0, 120, 20))
			.stringColor_(Color.yellow)
			.string_("COMPANDER")
			.align_('center');
		controlRow = GUI.hLayoutView.new(win, Rect.new(0, 0, fullWidth, 80))
		    .background_(Color.blue(0.5, alpha:0.2));
		EZJKnob.new(controlRow, Rect.new(0, 0, 40, 80), "thresh")
			.knobColor_(knobColors)
			.value_(threshold)
			.stringColor_(Color.yellow);
		EZJKnob.new(controlRow, Rect.new(0, 0, 40, 80), "sl-bel")
			.spec_([0.001, 2, 2].asSpec)
			.knobColor_(knobColors)
			.value_(slopeBelow)
			.stringColor_(Color.yellow);
		EZJKnob.new(controlRow, Rect.new(0, 0, 40, 80), "sl-ab")
			.spec_([0.001, 2, 2].asSpec)
			.knobColor_(knobColors)
			.value_(slopeAbove)
			.stringColor_(Color.yellow);
		EZJKnob.new(controlRow, Rect.new(0, 0, 40, 80), "clampT")
			.spec_([0.0001, 0.3, 2].asSpec)
			.knobColor_(knobColors)
			.value_(clampTime)
			.stringColor_(Color.yellow);
		EZJKnob.new(controlRow, Rect.new(0, 0, 40, 80), "relaxT")			.spec_([0.0001, 0.3, 2].asSpec)
			.knobColor_(knobColors)
			.value_(relaxTime)
			.stringColor_(Color.yellow);
	}
}