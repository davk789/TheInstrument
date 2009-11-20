Vodocer : EffectBase {
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
		synthdefName = 'vodocer';
		winBounds = Rect.new(winBounds.left, winBounds.top, 450, 275);
		bus = parent.mixer.channels[inputName].inBus;
		startParams = Dictionary[
			'bus'     -> bus,
			'mix'     -> 1,
			'numBands'    -> 1,
			'low'    -> 220, 
			'high' -> 1, 
			'q' -> bus,
			'inLev'  -> bus,
			'carLev'  -> 0, 
			'hpfscal'  -> 0.1,
			'hpfCutoff' -> 3000,
			'outscal'   -> 0
		];
		this.setGUIControls;
		this.startSynth;
	}
	
	*loadSynthDef { |s|
		s = s ? Server.default;
		
		SynthDef.new("vodocer", { |bus=22, mix=1, numBands=32, low=100, high=5000, q=0.02, inLev=1, carLev=1, hpfscal=0.05, hpfCutoff=3000, outscal=25|
			var aSig, aIn, aVoc, aCar, aMod, kFreq, hasFreq;
			aIn = In.ar(bus) * inLev;
			# kFreq, hasFreq = Tartini.kr(aMod);
			aCar = LFTri.ar(kFreq) * carLev;
			aVoc = Vocoder.ar(aCar, aMod, numBands, low, high, q, hpfCutoff, hpfscal, outscal); //bark(aCar, aMod);
			aSig = (aIn * (mix - 1).abs) + (aIn * mix);
			ReplaceOut.ar(bus, Pan2.ar(aSig, 0));
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
			

		};
	}
	



}
