/*FMDisto {
	*new { 
	 // dummy
	}
}*/
FMDisto : EffectBase {
	var cutoffSpec, cutoffModSpec, server;
	*new { |par, group, name, ind|
		^super.new(par, group, name, ind).init_simplefilter;
	}
	
	init_simplefilter {
		var bus;
		cutoffSpec = [-12,12].asSpec;
		cutoffModSpec = [0, 8].asSpec;
		synthdefName = 'fmDisto';
		winBounds = Rect.new(winBounds.left, winBounds.top, 350, 215);
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
	
	*loadSynthDef { |s|
		s = s ? Server.default;
		SythDef.new("fmDisto", { |bus, modAmt=0, waveform=0, freq=440, mix=1|
			var aIn, aSig, asOsc, aFreq, aOutMix;
			aIn = In.ar(bus);
			aFreq = freq;
			asOsc = Select.ar(waveform, [LFCub.ar(aFreq), LFPar.ar(aFreq), LFTri.ar(aFreq), Pulse.ar(aFreq, 0.5)]);
			aSig = asOsc;
			aOutMix = (aIn * (mix - 1).abs) + (aSig * mix);
			Out.ar(bus, aOutMix.softclip);
		}).send(s);
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