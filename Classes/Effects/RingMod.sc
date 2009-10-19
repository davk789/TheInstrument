RingMod {
	*new {
		//dummy
	}
}

RingMod : EffectBase {
	var cutoffSpec, cutoffModSpec, dbSpec, server;
	/* a selection of filters that use the same types of arguments: 
		in, freq rez, aka lowpass highpass and bandpass filters */
	*new { |par, group, name, ind|
		^super.new(par, group, name, ind).init_ringmod;
	}
	
	init_ringmod {
		var bus;
		dbSpec = [-60, 24].asSpec;
		cutoffSpec = [-12,12].asSpec;
		cutoffModSpec = [0, 8].asSpec;
		synthdefName = 'ringMod';
		//winBounds = Rect.new(winBounds.left, winBounds.top, 275, 180);
		//Platform.case('linux', {
			winBounds = Rect.new(winBounds.left, winBounds.top, 350, 215);
		//});
		bus = parent.mixer.channels[inputName].inBus;
		startParams = Dictionary[
			'gain'    -> 1,
			'freq'    -> 220, 
			'ringAmt' -> 1, 
			'ringBus' -> bus,
			'modBus'  -> bus,
			'modAmt'  -> 0, 
			'modLag'  -> 0.1, 
			'shape'   -> 0, 
			'modAmt'  -> 0
		];
		this.setGUIControls;
		this.startSynth;
	}
	
	*loadSynthDef { |s|
		s = s ? Server.default;
		SynthDef.new("ringMod", { |gain=1,freq=220,ringAmt=1, ringBus=22, modBus=22, modAmt=0, modLag=0.1, shape=0, modAmt=0|
			var aSig, aIn, aModIn, aRingIn;
			aIn = In.ar(bus) * gain;
			aModIn = In.ar(modBus);
			aRingIn = In.ar(ringBus);
			aFreq = freq + Lag.ar(aModIn * modAmt, modLag);
			aOsc = Select.ar(shape, [LFCub.ar(aFreq), LFPar.ar(aFreq), LFTri.ar(aFreq), Pulse.ar(aFreq, 0.5),  aRingIn]) * modAmt;
			aSig = aIn * aOsc;
			Out.ar(bus, aSig.softclip);
		}).load(s);
	}
	
	setGUIControls {
		addGUIControls = {
			paramControls = paramControls.add(
				'gain' -> EZJKnob.new(win, Rect.new(0, 0, 50, 100), "gain")
					.knobAction_({ |val| this.setGain(val.dbAmp); })
				    .spec_(dbSpec)
					.knobColor_([Color.black, Color.white, Color.grey.alpha_(0.3), Color.white])
					.stringColor_(Color.white)
				    .font_(parent.controlFont)
					.value_(startParams['resonance'])
			);
		};
	}

}