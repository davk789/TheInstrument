/*FMDisto {
	*new { 
	 // dummy
	}
}*/
FMDisto : EffectBase {
	var cutoffSpec, cutoffModSpec, server;
	/* a selection of filters that use the same types of arguments: 
		in, freq rez, aka lowpass highpass and bandpass filters */
	*new { |par, group, name, ind|
		^super.new(par, group, name, ind).init_simplefilter;
	}
	
	init_simplefilter {
		var bus;
		cutoffSpec = [-12,12].asSpec;
		cutoffModSpec = [0, 8].asSpec;
		synthdefName = 'simpleFilter';
		//winBounds = Rect.new(winBounds.left, winBounds.top, 275, 180);
		//Platform.case('linux', {
			winBounds = Rect.new(winBounds.left, winBounds.top, 350, 215);
		//});
		bus = parent.mixer.channels[inputName].inBus;
		startParams = Dictionary[
			'gain'       -> 1,
			'bus'        -> bus,
			'modBus'     -> bus,
			'modAmt'     -> 0,
			'modLag'     -> 0.2,
			'mix'        -> 1,
			'freq'       -> 440,
			'resonance'  -> 1
		];
		this.setGUIControls;
		this.startSynth;
	}
	
	*loadSynthDef { |filter, s|
		// this works because ThyInstrument calls this synth after initializing 
		// its member variables
		filter = filter ? ThyInstrument.filterUGens["RLPF"];
		s = s ? Server.default;
		SynthDef.new("simpleFilter", { |gain=1, bus=20, modBus=20, modAmt=0, modLag=0.2,
			mix=1, freq=440, resonance=1, gate=1|
			var aIn, aScaleIn, aModIn, aFreq, aSig, aOutMix, aEnv;
			aIn = In.ar(bus);
			aScaleIn = (aIn * gain).softclip;
			aModIn = Lag.ar(In.ar(modBus), modLag) * modAmt;
			aFreq = Lag.ar(freq + aModIn, 1);
			aSig = SynthDef.wrap(filter, Array.fill(filter.numArgs, {0}), [aIn, aFreq, resonance]);
			aOutMix = (aIn * (mix - 1).abs) + (aSig * mix);
			aEnv = EnvGen.ar(Env.asr(0.1, 1, 0.1), gate, doneAction:2);
			ReplaceOut.ar(bus, aOutMix * aEnv);
		}).load(s);
	}
	
	setMix { |val|
		startParams['mix'] = val;
		server.sendMsg('n_set', nodeID, 'mix', startParams['mix']);
	}

	setGain { |val|
		startParams['gain'] = val;
		server.sendMsg('n_set', nodeID, 'gain', startParams['gain']);
	}
	
	setModAmt { |val|
		startParams['modAmt'] = val;
		server.sendMsg('n_set', nodeID, 'modAmt', startParams['modAmt']);
	}

	setModLag { |val|
		startParams['modLag'] = val;
		server.sendMsg('n_set', nodeID, 'modLag', startParams['modLag']);
	}
	
	setFreq { |val|
		startParams['freq'] = val;
		server.sendMsg('n_set', nodeID, 'freq', startParams['freq']);
	}
	
	setResonance { |val|
		startParams['resonance'] = val;
		server.sendMsg('n_set', nodeID, 'resonance', startParams['resonance']);
	}
	
	setModBus { |bus|
		startParams['modBus'] = bus;//parent.audioBusRegister[name];
		server.sendMsg('n_set', nodeID, 'modBus', startParams['modBus']);
	}
	
	setFilterType { |name|
		startParams['filterType'] = name;
		server.sendMsg('n_set', nodeID, 'gate', 0);
		paramControls['resonance'].spec = parent.filterSpecs[startParams['filterType']];
		this.class.loadSynthDef(parent.filterUGens[startParams['filterType']]);
		AppClock.sched( 0.15, {
			server.sendMsg('n_free', nodeID);
			this.startSynth;
			nil;
		});
	}
	
	setGUIControls {
		addGUIControls = {
			paramControls = paramControls.add(
				'mix' -> EZJKnob.new(win, Rect.new(0, 0, 50, 100), "mix")
					.value_(startParams['mix'])
					.knobAction_({ |val| this.setMix(val); })
					.knobColor_([Color.black, Color.white, Color.grey.alpha_(0.3), Color.white])
					.stringColor_(Color.white)
					.knob.step_(0.005)
			);
 
			paramControls = paramControls.add(
				'gain' -> EZJKnob.new(win, Rect.new(0, 0, 50, 100), "gain")
					.value_(startParams['gain'])
					.spec_('amp'.asSpec)
					.knobAction_({ |val| this.setGain(val); })
					.knobColor_([Color.black, Color.white, Color.grey.alpha_(0.3), Color.white])
					.stringColor_(Color.white)
					.knob.step_(0.005)
			);
			
			paramControls = paramControls.add(
				'modAmt' -> EZJKnob.new(win, Rect.new(0, 0, 50, 100), "mod")
					.value_(startParams['modAmt'])
					.spec_([0, 300, 6].asSpec)
					.knobAction_({ |val| this.setModAmt(val); })
					.knobColor_([Color.black, Color.white, Color.grey.alpha_(0.3), Color.white])
					.stringColor_(Color.white)
					.knob.step_(0.005)
			);

			paramControls = paramControls.add(
				'modLag' -> EZJKnob.new(win, Rect.new(0, 0, 50, 100), "modLag")
					.value_(startParams['modLag'])
					.spec_([0, 12, 4].asSpec)
					.knobAction_({ |val| this.setModLag(val); })
					.knobColor_([Color.black, Color.white, Color.grey.alpha_(0.3), Color.white])
					.stringColor_(Color.white)
					.knob.step_(0.005)
			);


			paramControls = paramControls.add(
				'freq' -> EZJKnob.new(win, Rect.new(0, 0, 50, 100), "freq")
					.value_(startParams['freq'])
					.spec_('freq'.asSpec)
					.knobAction_({ |val| this.setFreq(val); })
					.knobColor_([Color.black, Color.white, Color.grey.alpha_(0.3), Color.white])
					.stringColor_(Color.white)
					.knob.step_(0.005)
			);
			
			paramControls = paramControls.add(
				'resonance' -> EZJKnob.new(win, Rect.new(0, 0, 50, 100), "rez")
					.knobAction_({ |val| this.setResonance(val); })
					.knobColor_([Color.black, Color.white, Color.grey.alpha_(0.3), Color.white])
					.stringColor_(Color.white)
					.spec_(parent.filterSpecs[startParams['filterType']])
					.value_(startParams['resonance'])
			);
			
			GUI.staticText.new(win, Rect.new(0, 0, 100, 25))
				.stringColor_(Color.white)
				.font_(parent.controlFont)
				.align_('right')
				.string_("mod source");
			paramControls = paramControls.add(
				'modBus' -> GUI.popUpMenu.new(win, Rect.new(0, 0, 150, 25))
					.stringColor_(Color.white)
					.font_(parent.controlFont)
					.items_(parent.audioBusRegister.keys.asArray)
					.background_(Color.clear)
					.action_({ |obj| this.setModBus(parent.audioBusRegister[obj.item]) });
			);

			GUI.staticText.new(win, Rect.new(0, 0, 100, 25))
				.stringColor_(Color.white)
				.font_(parent.controlFont)
				.align_('right')
				.string_("filter type");
			paramControls = paramControls.add(
				'filterType' -> GUI.popUpMenu.new(win, Rect.new(0, 0, 150, 25))
					.stringColor_(Color.white)
					.font_(parent.controlFont)
					.items_(parent.filterSpecs.keys.asArray)
					.background_(Color.clear)
					.action_({ |obj| this.setFilterType(obj.item) });
			);

			paramControls['modBus']
				.value_(
					paramControls['modBus'].items.indexOf(
						parent.audioBusRegister.findKeyForValue(startParams['modBus'])
					)
				);
			paramControls['resonance']
				.spec_(parent.filterSpecs[paramControls['modBus'].item])
				.value_(startParams['resonance'])
				.knob.step_(0.005);

		};
	}

}