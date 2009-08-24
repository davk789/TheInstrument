Compressor : EffectBase {
	*new { |par, group, name, ind|
		^super.new(par, group, name, ind).init_compressor;
	}
	init_compressor {
		var bus;
		bus = parent.mixer.channels[inputName].inBus;
		synthdefName = 'fx_compressor';
		paramControls = Dictionary.new;
		this.updateStartParams(bus);
		this.startSynth;
		this.setGUIControls;

	}
	updateStartParams { |bus|
		startParams = Dictionary['bus' -> bus, 'controlBus' -> bus, 
			'threshold' -> 0.6, 'slopeBelow' -> 1, 'slopeAbove' -> 0.2, 
			'clampTime' -> 0.005, 'relaxTime' -> 0.008, 'mix' -> 1];
	}
	setThreshold { |val|
		startParams['threshold'] = val;
		server.sendMsg('n_set', nodeID, 'threshold', startParams['threshold']);
	}
	setSlopeBelow { |val|
		startParams['slopeBelow'] = val;
		server.sendMsg('n_set', nodeID, 'slopeBelow', startParams['slopeBelow']);
	}
	setSlopeAbove { |val|
		startParams['slopeBelow'] = val;
		server.sendMsg('n_set', nodeID, 'slopeAbove', startParams['slopeBelow']);
	}
	setClampTime { |val|
		startParams['clampTime'] = val;
		server.sendMsg('n_set', nodeID, 'clampTime', startParams['clampTime']);
	}
	setRelaxTime { |val|
		startParams['relaxTime'] = val;
		server.sendMsg('n_set', nodeID, 'relaxTime', startParams['relaxTime']);
	}
	setControlBus { |val|
		startParams['controlBus'] = val;
		server.sendMsg('n_set', nodeID, 'controlBus', startParams['controlBus']);
	}
	setBypass { |val|
		if(val > 0){
			startParams['mix'] = 1;
		}{
			startParams['mix'] = -1;
		};
		server.sendMsg('n_set', nodeID, 'mix', startParams['mix']);
	}
	
	setGUIControls {
		var fullWidth, controlRow, knobColors, controlMenu;
		addGUIControls = {

			knobColors = [Color.white.alpha_(0.5), Color.yellow, Color.black, Color.yellow];
			fullWidth = win.view.bounds.width - 10;
			GUI.staticText.new(win, Rect.new(0, 0, 63, 20))
				.stringColor_(Color.yellow)
				.string_("sidechain: ")
				.align_('right');
			paramControls = paramControls.add(
				'controlBus' -> GUI.popUpMenu.new(win, Rect.new(0, 0, 130, 20))
					.background_(Color.clear)
					.stringColor_(Color.yellow)
					.items_(parent.audioBusRegister.keys.asArray)
					.action_({ |obj| this.setControlBus(parent.audioBusRegister[obj.item]); })
			);
			/*paramControls['controlBus'].value = 				controlMenu.items.indexOf(
					parent.audioBusRegister.findKeyForValue(startParams['controlBus'])
				);*/

			paramControls = paramControls.add(
				'mix' -> GUI.button.new(win, Rect.new(0, 0, 40, 20))
					.states_([
						["bypass", Color.black, Color.white(0.6).alpha_(0.6)],
						["bypass", Color.black, Color.yellow]
					])
					.action_({ |obj| this.setBypass(obj.value); })
			);				
				
			GUI.staticText.new(win, Rect.new(0, 0, 120, 20))
				.stringColor_(Color.yellow)
				.string_("COMPANDER")
				.align_('center');


			controlRow = GUI.hLayoutView.new(win, Rect.new(0, 0, fullWidth, 80))
			    .background_(Color.blue(0.5, alpha:0.2));
			paramControls = paramControls.add(
				'threshold' -> EZJKnob.new(controlRow, Rect.new(0, 0, 40, 80), "thresh")
					.knobColor_(knobColors)
					.value_(startParams['threshold'])
					.stringColor_(Color.yellow)
					.knobAction_({ |obj| this.setThreshold(obj.value); })
			);
			paramControls = paramControls.add(
				'slopeBelow' -> EZJKnob.new(controlRow, Rect.new(0, 0, 40, 80), "sl-bel")
					.spec_([0.001, 2, 2].asSpec)
					.knobColor_(knobColors)
					.value_(startParams['slopeBelow'])
					.stringColor_(Color.yellow)
					.knobAction_({ |obj| this.setSlopeBelow(obj.value); })
			);
			paramControls = paramControls.add(
				'slopeAbove' -> EZJKnob.new(controlRow, Rect.new(0, 0, 40, 80), "sl-ab")
					.spec_([0.001, 2, 2].asSpec)
					.knobColor_(knobColors)
					.value_(startParams['slopeAbove'])
					.stringColor_(Color.yellow)
					.knobAction_({ |obj| this.setSlopeAbove(obj.value); })
			);
			paramControls = paramControls.add(
				'clampTime' -> EZJKnob.new(controlRow, Rect.new(0, 0, 40, 80), "clampT")
					.spec_([0.0001, 0.3, 2].asSpec)
					.knobColor_(knobColors)
					.value_(startParams['clampTime'])
					.stringColor_(Color.yellow)
					.knobAction_({ |obj| this.setClampTime(obj.value); })
			);
			paramControls = paramControls.add(
				'relaxTime' -> EZJKnob.new(controlRow, Rect.new(0, 0, 40, 80), "relaxT")					.spec_([0.0001, 0.3, 2].asSpec)
					.knobColor_(knobColors)
					.value_(startParams['relaxTime'])
					.stringColor_(Color.yellow)
					.knobAction_({ |obj| this.setRelaxTime(obj.value); })
			);	
		}
	}
}