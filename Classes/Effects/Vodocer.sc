Vodocer : EffectBase {
	var modSpec, dbSpec, lagSpec;
	/* classic moog-type filterbank vocoder, but much worse */
	*new { |par, group, name, ind|
		^super.new(par, group, name, ind).init_ringmod;
	}
	
	init_ringmod {
		var bus;
		dbSpec = [-60, 24].asSpec;
		modSpec = [0, 6].asSpec;
		lagSpec = [0, 4].asSpec;
		synthdefName = 'vodocer';
		winBounds = Rect.new(winBounds.left, winBounds.top, 450, 225);
		bus = parent.mixer.channels[inputName].inBus;
		startParams = Dictionary[
			'bus'         -> bus,
			'mix'         -> 1,
			'numBands'    -> 16,
			'low'         -> 220, 
			'high'        -> 2200, 
			'q'           -> 0.2,
			'inLev'       -> 1,
			'carLev'      -> 1, 
			'hpfscal'     -> 0.1,
			'hpfCutoff'   -> 3000,
			'outscal'     -> 10,
			'carrierSource' -> 0,
			'carrierBus'  -> (bus + 2) // meh whatev
		];
		this.setGUIControls;
		this.startSynth;
	}
	
	*loadSynthDef { |s|
		s = s ? Server.default;
		
		SynthDef.new("vodocer", { 
			|bus=22, mix=1, numBands=32, low=100, high=5000, q=0.02, 
			 inLev=1, carLev=1, hpfscal=0.05, hpfCutoff=3000, outscal=25, 
			 carrierSource=0, carrierBus=22|
			var aSig, aIn, aCarIn, aVoc, asCar, kFreq, hasFreq;
			aIn = In.ar(bus) * inLev;
			aCarIn = In.ar(carrierBus);
			# kFreq, hasFreq = Tartini.kr(aIn);
			asCar = Select.ar(carrierSource, 
				[LFTri.ar(kFreq), 
				 LFCub.ar(kFreq), 
				 LFSaw.ar(kFreq), 
				 WhiteNoise.ar,
				 aCarIn]
			) * carLev;
			// replace this with my own code, to modulate the bands?
			aVoc = Vocoder.ar(asCar, aIn, numBands, low, high, q, hpfCutoff, hpfscal, outscal);
			aSig = (aIn * (mix - 1).abs) + (aVoc * mix);
			ReplaceOut.ar(bus, aSig);
		}).load(s);
	}
	
	
		
	setGUIControls {
		addGUIControls = {

			GUI.staticText.new(win, Rect.new(0, 0, 65, 25))
				.stringColor_(Color.white)
				.font_(parent.controlFont)
				.string_("num bands:");

			paramControls = paramControls.add(
				'numBands' -> GUI.popUpMenu.new(win, Rect.new(0, 0, 150, 25))
					.stringColor_(Color.white)
					.font_(parent.controlFont)
					.items_(Array.fill(16, { |i| ((i + 1) * 4).asString }))
					.background_(Color.clear)
					.value_(3)
					.action_({ |obj| 
						server.sendMsg('n_free', nodeID);
						this.setParam('numBands', obj.item.interpret);
						this.startSynth;
					});
			);

			GUI.staticText.new(win, Rect.new(0, 0, 65, 25))
				.stringColor_(Color.white)
				.font_(parent.controlFont)
				.string_("carrier source:");

			paramControls = paramControls.add(
				'carrierSource' -> GUI.popUpMenu.new(win, Rect.new(0, 0, 150, 25))
					.stringColor_(Color.white)
					.font_(parent.controlFont)
					.items_(["triangle", "cubic", "saw", "white noise", "input"])
					.background_(Color.clear)
					.value_(0)
					.action_({ |obj| this.setParam('carrierSource', obj.value) });
			);
			
			GUI.staticText.new(win, Rect.new(0, 0, 75, 25))
				.stringColor_(Color.white)
				.font_(parent.controlFont)
				.string_("ext. carrier bus:");

			paramControls = paramControls.add(
				'carrierBus' -> GUI.popUpMenu.new(win, Rect.new(0, 0, 150, 25))
					.stringColor_(Color.white)
					.font_(parent.controlFont)
					.items_(parent.audioBusRegister.keys.asArray)
					.background_(Color.clear)
					.value_(3)
					.action_({ |obj| this.setParam('carrierBus', parent.audioBusRegister[obj.item]) });
			);
			
			GUI.staticText.new(win, Rect.new(0, 0, 175, 25)).string_("");

			paramControls = paramControls.add(
				'mix' -> EZJKnob.new(win, Rect.new(0, 0, 40, 80), "mix")
					.knobAction_({ |val| this.setParam('mix', val); })
					.knobColor_([Color.black, Color.white, Color.grey.alpha_(0.3), Color.white])
					.stringColor_(Color.white)
				    .font_(parent.controlFont)
				    .centered_(true)
					.value_(startParams['mix'])
			);

			paramControls = paramControls.add(
				'low' -> EZJKnob.new(win, Rect.new(0, 0, 40, 80), "low")
					.knobAction_({ |val| this.setParam('low', val); })
					.knobColor_([Color.black, Color.white, Color.grey.alpha_(0.3), Color.white])
					.spec_('lofreq'.asSpec)
					.stringColor_(Color.white)
				    .font_(parent.controlFont)
					.value_(startParams['low'])
			);
			
			paramControls = paramControls.add(
				'high' -> EZJKnob.new(win, Rect.new(0, 0, 40, 80), "high")
					.knobAction_({ |val| this.setParam('high', val); })
					.knobColor_([Color.black, Color.white, Color.grey.alpha_(0.3), Color.white])
					.spec_('freq'.asSpec)
					.stringColor_(Color.white)
				    .font_(parent.controlFont)
					.value_(startParams['high'])
			);
			
			paramControls = paramControls.add(
				'q' -> EZJKnob.new(win, Rect.new(0, 0, 40, 80), "q")
					.knobAction_({ |val| this.setParam('q', val); })
					.knobColor_([Color.black, Color.white, Color.grey.alpha_(0.3), Color.white])
					.stringColor_(Color.white)
				    .font_(parent.controlFont)
					.value_(startParams['q'])
			);

			paramControls = paramControls.add(
				'inLev' -> EZJKnob.new(win, Rect.new(0, 0, 40, 80), "inLev")
					.knobAction_({ |val| this.setParam('inLev', val); })
					.knobColor_([Color.black, Color.white, Color.grey.alpha_(0.3), Color.white])
					.stringColor_(Color.white)
					.spec_([0, 100, 'amp'].asSpec)
				    .font_(parent.controlFont)
					.value_(startParams['inLev'])
			);

			paramControls = paramControls.add(
				'carLev' -> EZJKnob.new(win, Rect.new(0, 0, 40, 80), "carLev")
					.knobAction_({ |val| this.setParam('carLev', val); })
					.knobColor_([Color.black, Color.white, Color.grey.alpha_(0.3), Color.white])
					.stringColor_(Color.white)
				    .font_(parent.controlFont)
					.spec_([0, 100, 'amp'].asSpec)
					.value_(startParams['carLev'])
			);
			
			paramControls = paramControls.add(
				'hpfscal' -> EZJKnob.new(win, Rect.new(0, 0, 40, 80), "hpfscal")
					.knobAction_({ |val| this.setParam('hpfscal', val); })
					.knobColor_([Color.black, Color.white, Color.grey.alpha_(0.3), Color.white])
					.stringColor_(Color.white)
				    .font_(parent.controlFont)
				    .spec_('amp'.asSpec)
					.value_(startParams['hpfscal'])
			);

			paramControls = paramControls.add(
				'hpfCutoff' -> EZJKnob.new(win, Rect.new(0, 0, 40, 80), "hpfCutoff")
					.knobAction_({ |val| this.setParam('hpfCutoff', val); })
					.knobColor_([Color.black, Color.white, Color.grey.alpha_(0.3), Color.white])
					.stringColor_(Color.white)
				    .font_(parent.controlFont)
				    .spec_('freq'.asSpec)
					.value_(startParams['hpfCutoff'])
			);
			
			paramControls = paramControls.add(
				'outscal' -> EZJKnob.new(win, Rect.new(0, 0, 40, 80), "outscal")
					.knobAction_({ |val| this.setParam('outscal', val); })
					.knobColor_([Color.black, Color.white, Color.grey.alpha_(0.3), Color.white])
					.stringColor_(Color.white)
					.spec_([0, 100, 'amp'].asSpec)
				    .font_(parent.controlFont)
					.value_(startParams['outscal'])
			);

		};
	}
	



}
