PolyGendy : InstrumentVoice { // a challenge to myself to finish a small project
	var adParamButtons, ddParamButtons, freqRangeSlider, durScaleSlider, initCPsSlider,
		initCPsModulatorMenu, durScaleModulatorMenu, freqRangeModulatorMenu, envelopeView, envScaleSlider;
	var maxFreq=2;
	*new { |par|
		^super.new(par).init_polygendy;
	}

	init_polygendy {
		startParams = Dictionary[
			'ampdist'  -> 0,
			'durdist'  -> 0,
			'ddparam'  -> 1,
			'adparam'  -> 1,
			'minfreq'  -> 200,
			'maxfreq'  -> 400,
			'durscale' -> 1.0,
			'peakA'    -> 0.7,
			'peakB'    -> 0.3,
			'peakC'    -> 0.4,
			'att'      -> 0.04,
			'dec'      -> 0.2,
			'sus'      -> 0.3,
			'rel'      -> 0.2,
			'lev'      -> 1,
			'envScale' -> 1,
			'curve'    -> -1.6,
			'outBus'   -> outBus
		];
		synthDefName = 'PolyGendy';
		
		this.initMIDI;
		this.makeGUI;
		this.addMixerChannel;
		postln(this.class.asString ++ " initialized");
	}

	initMIDI {
		noteOnFunction = { |src,chan,num,val|
			startParams['minFreq'] = num.midicps; // maybe add support for microtunings?
			startParams['maxFreq'] = startParams['minFreq'] * maxFreq;
		};
	}
	
	setDDParam { |sel|
		this.setParam('ddParam', sel);
	}

	setADParam { |sel|
		this.setParam('adParam', sel);
	}

	setFreqRange { |val|
		maxFreq = val;
		startParams['maxFreq'] = maxFreq * startParams['minFreq'];
	}
	
	setInitCPs { |val|
		this.setParam('initCPs', val);
	}
	
	setDurScale { |val|
		this.setParam('durScale', val);
	}
	
	setEnvelope { |env|
		startParams['att'] = (env[0][1] - env[0][0]);
		startParams['dec'] = (env[0][2] - env[0][1]);
		startParams['sus'] = (env[0][3] - env[0][2]);
		startParams['rel'] = (env[0][4] - env[0][3]);
		startParams['peakA'] = env[1][1];
		startParams['peakB'] = env[1][2];
		startParams['peakC'] = env[1][3];
		server.sendMsg('n_set', groupID, 
			'att',   startParams['att'], 
			'dec',   startParams['dec'],
			'sus',   startParams['sus'], 
			'rel',   startParams['rel'], 
			'peakA', startParams['peakA'], 
			'peakB', startParams['peakB'], 
			'peakC', startParams['peakC']
		);
	}
	
	setEnvScale { |val|
		this.setParam('envScale', val);
	}

	*loadSynthDef {
		SynthDef.new(synthDefName, {
			|ampdist,durdist,adparam,ddparam,minfreq,maxfreq,durscale,initcps,mul,add
			 peakA=0.7, peakB=0.5, peakC=0.6, att=0.1, dec=0.2, sus=0.3, rel=0.2,
			 gate=0, lev=1, envScale=0, curve=(-1.6), outBus=22|
			var aOsc, aEnv;
			aOsc = Gendy1.ar(
				ampdist, /*Choice of probability distribution for the next perturbation of the amplitude of a control point. 
					The distributions are (adapted from the GENDYN program in Formalized Music):
					0- LINEAR
					1- CAUCHY
					2- LOGIST
					3- HYPERBCOS
					4- ARCSINE
					5- EXPON
					6- SINUS
				*/
				durdist, //Choice of distribution for the perturbation of the current inter control point duration
						 // presumably the same options as the ampdist arg
				adparam, /*A parameter for the shape of the amplitude probability distribution, requires values in the range 0.0001 to 1 						   (there are safety checks in the code so don't worry too much if you want to modulate!)*/
				ddparam, // A parameter for the shape of the duration probability distribution, requires values in the range 0.0001 to 1
				minfreq, //Minimum allowed frequency of oscillation for the Gendy1 oscillator, so gives the largest period the duration is allowed to take on. 
				maxfreq, //Maximum allowed frequency of oscillation for the Gendy1 oscillator, so gives the smallest period the duration is allowed to take on. 
				durscale, //Normally 0.0 to 1.0, multiplier for the distribution's delta value for duration. An ampscale of 1.0 allows the full range of  -1 to 1 for a change of duration.
				initcps, /*initCPs- Initialise the number of control points in the memory. Xenakis specifies 12. There would be this number of control points per cycle of the oscillator, though the oscillator's period will constantly change due to the duration distribution.  */
				mul,
				add);
			aEnv = EnvGen.ar(
				Env.new([0, peakA, peakB, peakC, 0], 
						[att, sus, dec, rel], 
						[curve, 0, 0, curve], 3), // applying curve only to attack and release
				gate, 
				lev, 
				0, 
				envScale, 
				2);
			Out.ar(outBus, aEnv * aOsc);
		}).load(server);
	}
	
	makeGUI {
		var probDists, midiSources, adParamView, ddParamView;
		var envScaleSpec, envView;
		win = GUI.window.new("PolyGendy", Rect.new(500.rand, 500.rand, 600, 300)).front;
		win.view.decorator = FlowLayout(win.view.bounds);

		probDists = ["linear","cauchy","logist","hyperbcos","arcsine","expon","sinus",];
		
		midiSources = ['*none*', 'mod wheel', 'aftertouch', 'bend', 'knob 1', 'knob 2', 'knob 3', 'knob 4', 'knob 5', 'knob 6', 'knob 7', 'knob 8',];

		adParamView = GUI.hLayoutView.new(win, Rect.new(0, 0, 600, 25));
		GUI.staticText.new(adParamView, Rect.new(0, 0, 60, 25))
			.string_("adparam");
		adParamButtons = Array.fill(7, { |ind|
			GUI.button.new(adParamView, Rect.new(0, 0, 60, 25))
				.font_(parent.controlFont)
				.states_([
					[probDists[ind], Color.blue(0.2), Color.white,],
					[probDists[ind], Color.white, Color.blue(0.2),],
				])
				.action_({ |obj|
					this.setADParam(ind);
					adParamButtons.do{ |o,i|
						if(i == ind){
							o.value = 1;	
						}{
							o.value = 0;
						};
					};
				})

		});
		adParamButtons[0].value = 1;

		ddParamView = GUI.hLayoutView.new(win, Rect.new(0, 0, 600, 25));
		GUI.staticText.new(ddParamView, Rect.new(0, 0, 60, 25))
			.string_("ddparam");
		ddParamButtons = Array.fill(7, { |ind|
			GUI.button.new(ddParamView, Rect.new(0, 0, 60, 25))
				.font_(parent.controlFont)
				.states_([
					[probDists[ind], Color.blue(0.2), Color.white,],
					[probDists[ind], Color.white, Color.blue(0.2),],
				])
				.action_({ |obj|
					this.setDDParam(ind);
					ddParamButtons.do{ |o,i|
						if(i == ind){
							o.value = 1;	
						}{
							o.value = 0;
						};
					};
				})
		});
		ddParamButtons[0].value = 1;
		//, durScaleSlider, initCPsSlider
		freqRangeSlider = EZSlider.new(
			parent: win, 
			bounds: Rect.new(0, 0, 500, 25),
			label:"freq range",
			controlSpec: [1, 4].asSpec,
			initVal: 200,
			action: { |obj| this.setFreqRange(obj.value) }
		);

		freqRangeModulatorMenu = GUI.popUpMenu.new(win, Rect.new(0, 0, 75, 25))
			.items_(midiSources)
			.action_({ |obj| "need a function".postln; });

		durScaleSlider = EZSlider.new(
			parent: win, 
			bounds: Rect.new(0, 0, 500, 25),
			label:"dur scale",
			initVal: 1,
			action: { |obj| this.setDurScale(obj.value) }
		);

		durScaleModulatorMenu = GUI.popUpMenu.new(win, Rect.new(0, 0, 75, 25))
			.items_(midiSources)
			.action_({ |obj| "need a function".postln; });
		
		initCPsSlider = EZSlider.new(
			parent: win, 
			bounds: Rect.new(0, 0, 500, 25),
			label:"init cps",
			controlSpec: [2, 24].asSpec,
			initVal: 12,
			round: 1,
			action: { |obj| this.setInitCPs(obj.value) }
		);

		initCPsModulatorMenu = GUI.popUpMenu.new(win, Rect.new(0, 0, 75, 25))
			.items_(midiSources ++ ["velocity"])
			.action_({ |obj| "need a function".postln; });
		
		envView = GUI.vLayoutView.new(win, Rect.new(0, 0, 150, 130));
		envelopeView = GUI.envelopeView.new(envView, Rect.new(0, 0, 150, 100))
			.value_([[0.0, 0.05, 0.15, 0.8, 1.0], [0.0, 0.99, 0.5, 0.65, 0.0]])
			.thumbSize_(5)
			.drawLines_(true)
			.setEditable(0, false)
			.setEditable(4, false)
			.action_({ |obj| this.setEnvelope(obj.value); });

		envScaleSpec = [0.1, 10, 2.2].asSpec;
		envScaleSlider = GUI.slider.new(envView, Rect.new(0, 0, 150, 25))
			.value_(envScaleSpec.unmap(1))
			.action_({ |obj| this.setEnvScale(envScaleSpec.map(obj.value)); });


	}

}
