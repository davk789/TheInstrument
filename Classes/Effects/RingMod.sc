RingMod : EffectBase {
	var modSpec, dbSpec, lagSpec;
	/* a ring modulator with a selection of modulate-able waveforms, and input multiplication */
	*new { |par, group, name, ind|
		^super.new(par, group, name, ind).init_ringmod;
	}
	
	init_ringmod {
		var bus;
		dbSpec = [-60, 24].asSpec;
		modSpec = [0, 6].asSpec;
		lagSpec = [0, 4].asSpec;
		synthdefName = 'ringMod';
		winBounds = Rect.new(winBounds.left, winBounds.top, 450, 275);
		bus = parent.mixer.channels[inputName].inBus;
		startParams = Dictionary[
			'bus'     -> bus,
			'mix'     -> 1,
			'gain'    -> 1,
			'freq'    -> 220, 
			'ringAmt' -> 1, 
			'ringBus' -> bus,
			'modBus'  -> bus,
			'modAmt'  -> 0, 
			'modLag'  -> 0.1, 
			'shape'   -> 0
		];
		this.setGUIControls;
		this.startSynth;
	}
	
	*loadSynthDef { |s|
		s = s ? Server.default;
		SynthDef.new("ringMod", { |mix=1, bus=22, gain=1, freq=220, ringAmt=1, ringBus=22, modBus=22, modAmt=0, modLag=0.1, shape=0|
			var aSig, aIn, aModIn, aRingIn, aFreq, aOsc, aOutMix;
			aIn = In.ar(bus) * gain;
			aModIn = In.ar(modBus);
			aRingIn = In.ar(ringBus);
			aFreq = freq + Lag.ar(aModIn * modAmt * freq, modLag);
			aOsc = Select.ar(shape, [LFCub.ar(aFreq), LFPar.ar(aFreq), LFTri.ar(aFreq), Pulse.ar(aFreq, 0.5),  aRingIn]) * ringAmt;
			aSig = aIn * aOsc;
			aOutMix = (aIn * (mix - 1).abs) + (aSig * mix);
			Out.ar(bus, aOutMix.softclip);
		}).load(s);
	}
		
	setGUIControls {
		addGUIControls = {
			paramControls = paramControls.add(
				'mix' -> EZJKnob.new(win, Rect.new(0, 0, 50, 100), "mix")
					.knobAction_({ |val| this.setParam('mix', val.dbamp); })
					.knobColor_([Color.black, Color.white, Color.grey.alpha_(0.3), Color.white])
					.stringColor_(Color.white)
				    .font_(parent.controlFont)
					.value_(startParams['mix'])
			);

			paramControls = paramControls.add(
				'gain' -> EZJKnob.new(win, Rect.new(0, 0, 50, 100), "gain")
					.knobAction_({ |val| this.setParam('gain', val.dbamp); })
				    .spec_(dbSpec)
					.knobColor_([Color.black, Color.white, Color.grey.alpha_(0.3), Color.white])
					.stringColor_(Color.white)
				    .font_(parent.controlFont)
					.value_(startParams['gain'].ampdb.max(-60))
			);

			paramControls = paramControls.add(
				'freq' -> EZJKnob.new(win, Rect.new(0, 0, 50, 100), "freq")
					.knobAction_({ |val| this.setParam('freq', val); })
				    .spec_('freq'.asSpec)
					.knobColor_([Color.black, Color.white, Color.grey.alpha_(0.3), Color.white])
					.stringColor_(Color.white)
				    .font_(parent.controlFont)
					.value_(startParams['freq'])
			);
			
			paramControls = paramControls.add(
				'ringAmt' -> EZJKnob.new(win, Rect.new(0, 0, 50, 100), "ring")
					.knobAction_({ |val| this.setParam('ringAmt', val.dbamp); })
				    .spec_(dbSpec)
					.knobColor_([Color.black, Color.white, Color.grey.alpha_(0.3), Color.white])
					.stringColor_(Color.white)
				    .font_(parent.controlFont)
					.value_(startParams['ringAmt'].ampdb.max(-60))
			);
			
			paramControls = paramControls.add(
				'modAmt' -> EZJKnob.new(win, Rect.new(0, 0, 50, 100), "mod")
					.knobAction_({ |val| this.setParam('modAmt', val); })
				    .spec_(modSpec)
					.knobColor_([Color.black, Color.white, Color.grey.alpha_(0.3), Color.white])
					.stringColor_(Color.white)
				    .font_(parent.controlFont)
					.value_(startParams['modAmt'])
			);

			paramControls = paramControls.add(
				'modlag' -> EZJKnob.new(win, Rect.new(0, 0, 50, 100), "lag")
					.knobAction_({ |val| this.setParam('modLag', val); })
				    .spec_(lagSpec)
					.knobColor_([Color.black, Color.white, Color.grey.alpha_(0.3), Color.white])
					.stringColor_(Color.white)
				    .font_(parent.controlFont)
					.value_(startParams['modLag'])
			);	
			
			GUI.staticText.new(win, Rect.new(0, 0, 50, 25))
				.stringColor_(Color.white)
				.font_(parent.controlFont)
				.string_("ring bus:");
			
			paramControls = paramControls.add(
				'ringBus' -> GUI.popUpMenu.new(win, Rect.new(0, 0, 150, 25))
					.stringColor_(Color.white)
					.font_(parent.controlFont)
					.items_(parent.audioBusRegister.keys.asArray)
					.background_(Color.clear)
					.action_({ |obj| this.setParam('ringBus', parent.audioBusRegister[obj.item]) });
			);
			
			
			GUI.staticText.new(win, Rect.new(0, 0, 50, 25))
				.stringColor_(Color.white)
				.font_(parent.controlFont)
				.string_("mod bus:");

			paramControls = paramControls.add(
				'modBus' -> GUI.popUpMenu.new(win, Rect.new(0, 0, 150, 25))
					.stringColor_(Color.white)
					.font_(parent.controlFont)
					.items_(parent.audioBusRegister.keys.asArray)
					.background_(Color.clear)
					.action_({ |obj| this.setParam('modBus', parent.audioBusRegister[obj.item]) });
			);
			
			GUI.staticText.new(win, Rect.new(0, 0, 150, 25))
				.stringColor_(Color.white)
				.font_(parent.controlFont)
				.string_("wave shape:");

			paramControls = paramControls.add(
				'shape' -> GUI.popUpMenu.new(win, Rect.new(0, 0, 150, 25))
					.stringColor_(Color.white)
					.font_(parent.controlFont)
					.items_(["cubic", "parabolic", "triangle", "square", "input"])
					.background_(Color.clear)
					.action_({ |obj| this.setParam('shape', obj.value) });
			);
		};
	}
	



}
