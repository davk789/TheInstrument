WavetableSynthGUI {
	// should the GUI elements get their own variable declarations?
	*new {
		^super.new.init_wavetablesynthgui;
	}

	init_wavetablesynthgui {
		postln(this.class.asString ++ " initialized");
	}
	 
	makeGUI {
		postln("this is the function that will create the GUI. but what else should it do??");
		/*
		win.bounds = Rect.new(win.view.bounds.left, win.view.bounds.top, win.view.bounds.width, win.view.bounds.height + 135);
		presetMenu.items_((saveRoot ++ sep ++ "*").pathMatch.collect{ |obj,ind| obj.split($/).last.asSymbol; });
		filterMidiRow = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width - 10, 25))
			.background_(Color.blue(0.1, alpha:0.2));
		cutoffMenu = GUI.popUpMenu.new(filterMidiRow, Rect.new(0, 0, 37.5, 0))
			.items_(midiListMenu)
			.action_({ |obj| this.addModulator(obj.value, obj.item, 'cutoff'); });
		cutoffModMenu = GUI.popUpMenu.new(filterMidiRow, Rect.new(0, 0, 37.5, 0))
			.items_(midiListMenu)
			.action_({ |obj| this.addModulator(obj.value, obj.item, 'cutoffMod'); });
		GUI.staticText.new(filterMidiRow, Rect.new(0, 0, 37.5, 0));
		cutoffModSourceButton = GUI.button.new(filterMidiRow, Rect.new(0, 0, 75, 0))
			.states_([["osc1 mod", Color.black, Color.blue(0.1, alpha:0.2)],["osc2 mod", Color.blue, Color.red(0.1, alpha:0.2)]])
			.action_({ |obj| this.setModSource(obj.value); });
		filterTypeMenu = GUI.popUpMenu.new(filterMidiRow, Rect.new(0, 0, 100, 0))
			.items_(parent.filterUGens.keys.asArray)
			.action_({ |obj| this.setFilterType(obj.item); });
		
		filterControlRow = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width - 10, 75))
			.background_(Color.blue(0.1, alpha:0.2));
		cutoffKnob = EZJKnob.new(filterControlRow, Rect.new(0, 0, 37.5, 73), "cutoff")
			.spec_(cutoffSpec)
			.knobColor_([Color.black, Color.green, Color.black, Color.green])
			.knobAction_({ |obj| this.setCutoff(obj.value); })
			.knobCentered_(true);
		cutoffModKnob = EZJKnob.new(filterControlRow, Rect.new(0, 0, 37.5, 73), "coMod")
			.spec_(cutoffModSpec)
			.knobColor_([Color.black, Color.green, Color.black, Color.green])
			.knobAction_({ |obj| this.setCutoffMod(obj.value); });
		rezKnob = EZJKnob.new(filterControlRow, Rect.new(0, 0, 37.5, 73), "rez")
			.value_(1)
			.knobColor_([Color.black, Color.green, Color.black, Color.green])
			.knobAction_({ |obj| this.setResonance(obj.value); });
		
		filterEnvRow = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width - 10, 25))
			.background_(Color.blue(0.1, alpha:0.2));
		cutoffEnvButton = GUI.button.new(filterEnvRow, Rect.new(0, 0, 34, 0))
			.states_([["env", Color.black, Color.clear],["env", Color.red, Color.yellow]])
			.action_({|obj| this.setCutoffFlag(obj.value) });
		cutoffModEnvButton = GUI.button.new(filterEnvRow, Rect.new(0, 0, 34, 0))
			.states_([["env", Color.black, Color.clear],["env", Color.red, Color.yellow]])
			.action_({|obj| this.setCutoffModFlag(obj.value) });
		*/
	}
}