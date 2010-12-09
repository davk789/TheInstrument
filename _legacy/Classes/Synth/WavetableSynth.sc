WavetableSynth : InstrumentVoice {
	classvar  classGroup=102;
	var parent, activeNotes, win, instGroup=103, s, bufferA=70, bufferB=71, tunings, tuning,
		<>att=0.001, <>dec=0.008, <>sus=0.1, <>rel=0.5, 
		<>peakA=1, <>peakB=0.6, <>peakC=0.63, <>mul=4, <>feedback=0, touch=0, <>lag=0.1, 
		pitchBend=1, <>outBus=19, <recorderID, pitchBend=1,
		trigMode=0, xfade=0, fbLag=0, fbMul=0, freq2=0, fmAmt=0, fbMulEnvFlag=0, freq2EnvFlag=0,
 		fmEnvFlag=0, envScale=1, midiCCSources, midiListMenu, 
		modulatorSources, currentModulators, xfadeKnob, fbMulKnob, freq2Knob, fm2Knob,
		noteOnCommand, noteOffCommand, <>saveRoot, sep, 
	    xfadeSpec, fbLagSpec, fbMulSpec, freq2Spec, fm2Spec, 
	    midiThru=false, midiOut,
		presetRow, presetNameField, saveButton, presetMenu, modeRow, modeMenu, fbLagKnob, partialRow1, partialAAmps, partialAFreqs, pr2AuxControls, xFadeMenu, fbMulMenu, freq2Menu, fm2Menu, partialRow2, syncModeMenu, partialBAmps, partialBFreqs, envelopeView, waveformDraw, targetColumn, targetAButton, targetBButton, pr2EnvRow, fbMulEnvButton, freq2EnvButton, fm2EnvButton, envScaleSlider, envScaleSpec, bendButton;
	// 	16 18 12 17 19 13 // transport cc
	// 72  8 74 71  20 22 86 73 //   cc numbers 
	*new { |par, midi|
		^super.new.init_wavetablesynth(par, midi);
	}

	init_wavetablesynth { |par, midi|
		s = Server.default;
		parent = par;
		if(midi.notNil){ midiThru = midi; };
		activeNotes = Dictionary.new;
		sep = Platform.pathSeparator;
		saveRoot = Platform.userAppSupportDir ++ sep ++ "Presets" ++ sep ++ "WavetableSynth";
		recorderID = "Wavetable Synth";
		modulatorSources = Dictionary[
			'mod wheel'-> [], 
			'aftertouch'-> [], 
			'bend'-> [], 
			'knob 1'-> [], 
			'knob 2'-> [], 
			'knob 3'-> [], 
			'knob 4'-> [], 
			'knob 5'-> [], 
			'knob 6'-> [], 
			'knob 7'-> [], 
			'knob 8'-> []
		];
		tuning = 'centaur';
		tunings = Dictionary[
			'ezra-I-VI-VII' -> [[0, 0.066666666666667, 0.125, 0.18518518518519, 0.25, 0.33333333333333, 0.4, 0.5, 0.58024691358025, 0.66666666666667, 0.77777777777778, 0.875], 12],
			'bohlen-pierce-eq' -> [[0, 1.4630, 2.9261, 4.3891, 5.8522, 7.3152, 8.7783, 10.2413, 11.7044, 13.1674, 14.6305, 16.0935, 17.5566], 19],
			'bohlen-pierce-just' -> [[0, 1.3324, 3.0185, 4.3508, 5.8251, 7.3693, 8.8436, 10.1760, 11.6502, 13.1944, 14.6687, 16.0011, 17.6872], 19],
			// dropping the top note of the scale to fit the 12-tone keyboaRd
			'bohlen-pierce-eq-trunc' -> [[0, 1.4630, 2.9261, 4.3891, 5.8522, 7.3152, 8.7783, 10.2413, 11.7044, 13.1674, 14.6305, 16.0935], 19],
			'bohlen-pierce-just-trunc' -> [[0, 1.3324, 3.0185, 4.3508, 5.8251, 7.3693, 8.8436, 10.1760, 11.6502, 13.1944, 14.6687, 16.0011], 19],
			'partch-full' -> [[0, 1.506, 1.65, 1.824, 2.039, 2.312, 2.669, 3.156, 3.474, 3.863, 4.175, 4.351, 4.98, 5.513, 5.825, 6.175, 6.487, 7.02, 7.649, 7.825, 8.137, 8.526, 8.844, 9.331, 9.688, 9.961, 10.176, 10.35, 10.494] + 12, 12],
			'partch-trunc-1' -> [[0, 1.5063705850064, 2.0391000173078, 3.1564128700056,
				4.1750796410438, 4.9804499913462, 6.1748780739572, 7.019550008654, 
				8.1368628613517, 9.3312909439627, 10.175962878659, 10.493629414994], 12],
			'centaur' -> [[0, 0.844671934697, 2.039100017308, 2.668709056037, 
				3.863137138648, 4.980449991346, 5.825121926043, 7.019550008654, 
				7.649159047383, 8.843587129994, 9.688259064691, 10.882687147302], 12],
			'indian_kalyan' -> [[0.0, 0.0, 1.8, 1.8, 3.9, 3.9, 5.75, 6.9, 6.9, 9.0, 9.0, 10.9], 12],
			'arabic_common' -> [[0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 5.5, 7.0, 8.0, 9.0, 10.0, 10.5], 12],
			'chinese_zhou_pent' -> [[0.0, 0.0, 2.04, 2.04, 2.04, 4.98, 4.98, 7.02, 7.02, 9.06, 9.06, 9.06], 12],
			'al_farabi_syn_chrom' -> [[0.0, 1.12, 2.31, 2.31, 2.31, 4.98, 4.98, 7.02, 8.14, 9.33, 9.33, 9.33], 12],
			'aristoxenos_intense_diatonic' -> [[0.0, 0.89, 0.89, 2.81, 2.81, 4.98, 4.98, 7.02, 7.91, 7.91, 9.83, 9.83], 12],
			'iranian_safi_a_ddin' -> [[0.0, 0.0, 2.21, 2.21, 4.42, 4.9, 4.9, 7.1, 7.1, 9.31, 9.79, 9.79], 12],
			'scottish' -> [[0.45, 0.45, 2.56, 2.56, 4.31, 5.5, 5.5, 7.5, 7.5, 9.31, 10.22, 10.22], 12],
			'indian_shatukeshi' -> [[0.0, 0.0, 2.3, 2.3, 3.85, 4.92, 4.92, 6.9, 7.8, 7.8, 10.15, 10.15], 12],
			'shur_adhami' -> [[0.0, 1.68, 1.58, 2.82, 2.82, 5.0, 5.0, 7.0, 7.0, 9.68, 9.58, 11.0], 12],
			'indian_kafi' -> [[0.0, 0.0, 1.7, 2.7, 2.7, 5.0, 5.0, 6.75, 6.75, 8.9, 10.25, 10.25], 12],
			'afro_2' -> [[0.52, 0.52, 0.52, 3.25, 3.25, 4.5, 6.22, 6.22, 8.31, 8.31, 10.22, 10.22], 12],
			'afro_1' -> [[0.15, 0.5, 2.45, 2.62, 2.62, 4.93, 4.93, 7.1, 7.29, 7.29, 8.56], 12],
			'arabian' -> [[-0.0526315569878, 1.44736838341, 1.9736841917, 2.88157892227, 
				3.48684209585, 4.92105263472, 6.43421053886, 6.96052634716, 
				8.4736841917, 9.0, 9.90789473057, 10.5131579041], 12],
			'chinese_scholars_lute' -> [[0.0, 0.0, 2.31, 3.16, 3.86, 4.98, 4.98, 7.02, 7.02, 8.84, 8.84, 8.84], 12],
			'javanese_slendro' -> [[0.0, 0.0, 2.43, 2.43, 4.87, 4.87, 4.87, 7.3, 7.3, 9.73, 9.73, 12.17], 12],
			'japanese_linus_liu' -> [[0.0, 0.0, 2.04, 2.04, 4.08, 5.2, 5.2, 7.02, 7.02, 9.06, 11.1, 12.22], 12],
			'ho_mai_nhi' -> [[0.0, 0.0, 1.65, 1.65, 1.65, 4.98, 4.98, 7.02, 7.02, 8.67, 8.67, 8.67], 12],
			'mohajeri_shahin' -> [[0.0, 1.38, 1.38, 1.38, 3.75, 5.38, 5.38, 7.25, 7.25, 9.38, 9.38, 9.38], 12],
			'indian_observed_mode' -> [[0.0, 0.9, 0.9, 0.9, 3.66, 4.93, 4.93, 7.07, 7.81, 7.81, 7.81, 10.8], 12],
			'egyptian' -> [[0.0, 1.19736838341, 1.9736841917, 2.88157892227, 
				3.75, 5.0, 6.2763158083, 7.14473688602, 
				8, 9.55263161659, 10.0, 10.5131579041], 12],
			'indian_rajasthan' -> [[0.0, 0.0, 2.04, 2.04, 3.86, 4.98, 4.98, 7.02, 7.02, 7.02, 7.02, 10.88], 12],
			'homayoun_adhami' -> [[0.0, 0.0, 2.0, 1.58, 3.58, 5.0, 5.0, 7.0, 8.12, 7.12, 9.0, 11.0], 12],
			'persian_1' -> [[0.0, 1.1, 2.0, 3.0, 4.0, 5.0, 6.0, 7.12, 8.12, 9.1, 10.1, 11.15], 12],
			'persian_2' -> [[0.0, 0.85, 1.85, 2.85, 4.0, 5.2, 6.1, 7.15, 8.1, 9.15, 10.1], 12],
			'persian_3' -> [[0.0, 1.1, 2.1, 2.9, 3.9, 5.0, 6.0, 7.0, 8.0, 8.8, 10.05, 11.1], 12],
			'indian_sruthi' -> [[0.0, 1.10526311398, 2.0263158083, 3.10526311398, 
				3.85526317358, 4.9736841917, 5.89473682642, 7.0, 
				8.13157892227, 8.84210526943, 10.1710526943, 10.8815789223], 12],
			'al_farabi_dorian' -> [[0.0, 1.33, 1.33, 3.16, 3.16, 4.98, 4.98, 7.02, 8.35, 8.35, 10.18, 10.18], 12],
			'chinese_lu' -> [[0.0, 1.0, 2.0, 3.03947365284, 4.15789473057, 4.93421053886, 6.0, 7.07894730568, 8.0, 9.0, 10.0, 11.0], 12],
			'arabic_1' -> [[0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 10.5], 12],
			'arabic_3' -> [[0.0, 1.0, 2.0, 3.0, 3.5, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 10.5], 12],
			'arabic_2' -> [[0.0, 1.0, 2.0, 3.0, 3.5, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0], 12],
			'arabic_5' -> [[0.0, 1.0, 2.0, 3.0, 3.5, 5.0, 6.0, 7.0, 8.0, 8.5, 10.0, 11.0], 12],
			'arabic_4' -> [[0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 5.5, 7.0, 8.0, 9.0, 10.0, 10.5], 12],
			'arabic_7' -> [[0.0, 1.0, 1.5, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 8.5, 10.0, 11.0], 12],
			'arabic_6' -> [[-0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 5.5, 7.0, 8.0, 9.0, 10.0, 11.0], 12],
			'chinese_wang_po' -> [[0.0, 0.0, 2.04, 2.04, 4.03, 4.03, 6.09, 7.02, 7.02, 9.04, 11.06, 11.81], 12],
			'syrian_tawfiq_as_sabbagh' -> [[0.0, 0.0, 2.04, 2.04, 3.57, 4.98, 4.98, 7.02, 7.02, 8.55, 9.96, 9.96], 12],
			'12tet' -> [[0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 11.0], 12],
			'chinese_heptatonic' -> [[0.0, 0.0, 2.04, 2.04, 4.08, 4.98, 4.98, 7.02, 7.02, 9.06, 9.06, 11.1], 12],
			'iraq_8_tone' -> [[0.0, 0.0, 1.8, 1.8, 3.84, 4.98, 4.98, 6.78, 6.78, 8.82, 9.96, 9.96], 12],
			'west_african' -> [[0.0, 1.51315784454, 1.51315790415, 2.86842107773, 
				4.32894730568, 4.34210526943, 6.23684215546, 6.23684209585, 
				7.90789473057, 9.40789473057, 9.39473682642, 12.0], 12],
			'indian_mukund' -> [[0.0, 1.32894730568, 2.03947365284, 3.15789473057, 
				3.93421053886, 4.9736841917, 6.23684215546, 7.0, 8.35526311398, 
				9.06578946114, 10.1842105389, 10.9605263472], 12],
			'japanese_koto_pent' -> [[0.0, 0.0, 1.85, 3.37, 3.37, 3.37, 3.37, 6.83, 7.9, 8.0, 8.0, 8.0], 12],
			'iranian_mokhalif' -> [[0.0, 0.0, 2.0, 2.0, 4.0, 5.0, 5.0, 7.0, 8.4, 8.4, 10.4, 10.4], 12],
			'persian_4' -> [[0.0, 0.9, 2.0, 3.0, 3.85, 5.0, 6.0, 6.9, 8.05, 9.0, 10.15, 11.25], 12],
			'arabic_segah' -> [[0.0, 0.0, 2.0, 3.5, 3.5, 5.0, 5.0, 7.0, 7.0, 9.0, 10.5, 10.5], 12]
		];
		currentModulators = Dictionary['xFade' -> nil, 'fbMul' -> nil, 'freq2' -> nil, 'fm2' -> nil, 'cutoff' -> nil, 'cutoffMod' -> nil];
		midiCCSources = Dictionary[1 -> 'mod wheel', 72 ->  'knob 1', 8 -> 'knob 2', 74 -> 'knob 3', 71 -> 'knob 4', 20 -> 'knob 5', 22 -> 'knob 6', 86 -> 'knob 7', 73 -> 'knob 8'];
		midiListMenu = ['*none*', 'mod wheel', 'aftertouch', 'bend', 'knob 1', 'knob 2', 'knob 3', 'knob 4', 'knob 5', 'knob 6', 'knob 7', 'knob 8'];
		noteOnCommand = { |num,vel,pitch|
			s.sendMsg('s_new', 's_dualWavetable', activeNotes[num].last, 0, instGroup,
				'outBus', outBus, 'freq1', pitch, 'lev', (vel / 127).pow(2.2),
				'peakA', peakA, 'peakB', peakB, 'peakC', peakC,  'bufferA', bufferA, 'bufferB', bufferB,
				'att', att, 'dec', dec, 'sus', sus, 'rel', rel, 
				'trigMode', trigMode, 'xfade', xfade, 
				'fbLag', fbLag, 'fbMul', fbMul, 'freq2', freq2, 'fmAmt', fmAmt, 
				'fbMulEnvFlag', fbMulEnvFlag, 'freq2EnvFlag', freq2EnvFlag, 'fmEnvFlag', fmEnvFlag, 
				'envScale', envScale, 'bend', pitchBend);
			s.sendMsg('n_set', activeNotes[num].last, 'gate', 1);
		};
		noteOffCommand = { |id|
			s.sendMsg('n_set', id, 'gate', 0);
		};
		xfadeSpec = 'pan'.asSpec;
		fbLagSpec = [0.001, 4, 2.3].asSpec;
		fbMulSpec = [0, 256].asSpec;
		freq2Spec = [-12, 12].asSpec;
		fm2Spec = [0, 12].asSpec;
		if(midiThru){
			midiOut = MIDIOut.new(2);
		};
		s.sendMsg('g_new', classGroup, 0, 1);
		s.sendMsg('b_alloc', bufferA, 1024);
		s.sendMsg('b_gen', bufferA, 'sine2', 4, 1, 1);
		s.sendMsg('b_alloc', bufferB, 1024);
		s.sendMsg('b_gen', bufferB, 'sine2', 4, 1, 1);
		s.sendMsg('g_new', instGroup, 0, classGroup);

		this.addMixerChannel;
		this.initLooper;
		this.initGUI;
		

	}
	getParams  {
		// outBus should not be changed when the preset is loaded
		^[instGroup, outBus, peakA, peakB, peakC, bufferA, bufferB, att, dec, sus, rel, trigMode, xfade, fbLag, fbMul, freq2, fmAmt, fbMulEnvFlag, freq2EnvFlag, envScale, pitchBend, partialAFreqs.value, partialAAmps.value, partialBFreqs.value, partialBAmps.value, waveformDraw.value, targetAButton.value, targetBButton.value, xFadeMenu.value, fbMulMenu.value, freq2Menu.value, fm2Menu.value, syncModeMenu.value, xfadeKnob.value, fbLagKnob.value, fbMulKnob.value, freq2Knob.value, fm2Knob.value, envelopeView.value, bendButton.value, fbMulEnvButton.value, freq2EnvButton.value, fm2EnvButton.value, envScaleSlider.value];
	}
	setParams { |values|
		instGroup = values[0];
		// TODO !!! must make the WavetableSynth load and save presets sensibly !!!
		//outBus = values[1];
		peakA = values[2];
		peakB = values[3];
		peakC = values[4];
		bufferA = values[5];
		bufferB = values[6];
		att = values[7];
		dec = values[8];
		sus = values[9];
		rel = values[10];
		trigMode = values[11];
		xfade = values[12];
		fbLag = values[13];
		fbMul = values[14];
		freq2 = values[15];
		fmAmt = values[16];
		fbMulEnvFlag = values[17];
		freq2EnvFlag = values[18];
		envScale = values[19];
		pitchBend = values[20];
		partialAFreqs.value = values[21];
		partialAAmps.value = values[22];
		this.generatePartials(partialAFreqs.value, partialAAmps.value, bufferA);
		partialBFreqs.value = values[23];
		partialBAmps.value = values[24];
		this.generatePartials(partialBFreqs.value, partialBAmps.value, bufferB);
		waveformDraw.valueAction = values[25];
		targetAButton.valueAction = values[26];
		targetBButton.valueAction = values[27];
		xFadeMenu.valueAction = values[28];
		fbMulMenu.valueAction = values[29];
		freq2Menu.valueAction = values[30];
		fm2Menu.valueAction = values[31];
		syncModeMenu.valueAction = values[32];
		xfadeKnob.value = values[33];
		fbLagKnob.value = values[34];
		fbMulKnob.value = values[35];
		freq2Knob.value = values[36];
		fm2Knob.value = values[37];
		envelopeView.value = values[38];
		bendButton.valueAction = values[39];
		fbMulEnvButton.valueAction = values[40];
		freq2EnvButton.valueAction = values[41];
		fm2EnvButton.valueAction = values[42];
		envScaleSlider.value = values[43];
	}
	savePreset { |name|
		var fileName, filePath, fh, pipe;
		fileName = name ? Date.localtime.stamp;
		filePath = saveRoot ++ sep ++ fileName;
		fh = File.new(filePath, "w");
		if(fh.isOpen){
			fh.write(this.getParams.asInfString);
			fh.close;
		}{
			postln("creating save directory " ++ saveRoot);
			pipe = Pipe.new("mkdir -p \"" ++ saveRoot ++ "\"", "w");
			pipe.close;
			fh = File.new(filePath, "w");
			if(fh.isOpen){
				fh.write(this.getParams.asInfString);
				fh.close
			}{
				postln("preset save operation failed");
			};
			
		};
	}
	loadPreset { |presetName|
		var preset;
		preset = (saveRoot ++ sep ++ presetName).load;
		this.setParams(preset);
	}
	initLooper {
		//postln("calling parent.eventLooper.addChannel(1, " ++ recorderID ++ "); from " ++ this.class.asString);
		parent.eventLooper.addChannel(1, recorderID, instGroup);
		parent.eventLooper.channels[recorderID].action = { |values,index|
			//postln("back to a function defined in WavetableSynth the values are " ++ values);
			switch(values[0],
				0, {
					this.doNoteOn(values[1], values[2], values[3], values[4]);
				},
				1, {
					this.doNoteOff(values[1], values[2], values[3], values[4]);
				},
				2, {
					this.doCC(values[1], values[2], values[3], values[4]);
				},
				3, {
					this.doAfterTouch(values[1], values[2], values[3]);
				},
				4, {
					this.doBend(values[1], values[2], values[3]);
				}
			);
		};
	}
	looper {
		^parent.eventLooper.channels[recorderID];
	}
	looperHandleCC { |src,chan,num,val|
	    case{[1, 72, 8, 74, 71, 20, 22, 86, 73].includes(num)}{
	        this.looper.addEvent([2,src,chan,num,val]);
	    }
	    { num == 16 }{
	        this.looper.clear;
	    }
	    { num == 18}{
	        if(this.looper.eventCollection.notNil){
				defer{ 
					this.looper.seqMenu.value = (this.looper.seqMenu.value - 1) % this.looper.seqMenu.items.size;
	            	this.looper.load(this.looper.seqMenu.value);
				};
	        };
	    }
	    { num == 12 }{
	        if(this.looper.eventCollection.notNil){
	            defer{
					var menu;
					this.looper.seqMenu.value = (this.looper.seqMenu.value + 1) % this.looper.seqMenu.items.size;
	            	this.looper.load(this.looper.seqMenu.value);
	            };
	        };
	    }
	    { num == 17 }{
	        this.looper.stop;
	        { this.looper.playButton.value = 0; }.defer;
	    }
	    { num == 19 }{
	        this.looper.start;
	        { this.looper.playButton.value = 1; }.defer;
	    }
	    { num == 13 }{
			if(val > 0){
	             this.looper.startRecording;
	             { this.looper.recordButton.value = 1; }.defer;
	         }{
	             this.looper.stopRecording;
	             { this.looper.recordButton.value = 0; }.defer;
	         };
	    };
	}
	setSave { |name|
		if((name.isNil) || (name == "<>") || ((saveRoot ++ sep ++ "*").pathMatch.includes(saveRoot ++ name))){
			this.savePreset;
		}{
			this.savePreset(name);
		};
		presetNameField.string_("<>");
		presetMenu.items = (saveRoot ++ sep ++ "*").pathMatch.collect{ |obj,ind| obj.split($/).last; }
	}
	setTuning { |choice|
		tuning = choice;
	}
	generatePartials { |freqs, amps, buffer|
		var sFrequency, sAmplitude;
		sFrequency = freqs * Array.series(19, 2, 2);
		sAmplitude = amps.pow(3);
		s.listSendMsg(['b_gen', buffer, 'sine2', 5] ++ [sFrequency, sAmplitude].lace(38));
	}
	drawWaveform { |sliders, index, buffer|
		s.sendMsg('b_setn', buffer, index * 4, 4, sliders[index], sliders[index], sliders[index], sliders[index]);
	}
	setSyncMode { |flag|
		trigMode = flag;
		s.sendMsg('n_set', instGroup, 'trigMode', trigMode);
	}
	setPitchBend { |bend|
		var bScaled;
		bScaled = bend * 2;
		if(bScaled > 1){
			pitchBend = ((bScaled - 1) * 2) + 1;
		}{
			pitchBend = bScaled.pow(2);
		};
		s.sendMsg('n_set', instGroup, 'bend', pitchBend);
	}
	setXFade { |amt|
		xfade = amt;
		s.sendMsg('n_set', instGroup, 'xfade', xfade);
	}
	setFBLag { |amt|
		fbLag = amt;
		s.sendMsg('n_set', instGroup, 'fbLag', fbLag);
	}
	setFBMul { |mul|
		fbMul = mul;
		s.sendMsg('n_set', instGroup, 'fbMul', fbMul);
	}
	setFreq2 { |frq|
		freq2 = this.octaveToRatio(frq);
		//freq2 = frq + 1;
		s.sendMsg('n_set', instGroup, 'freq2', freq2);
	}
	setFM2 { |fm|
		fmAmt = fm;
		s.sendMsg('n_set', instGroup, 'fmAmt', fmAmt);
	}
	setFBMulEnvFlag { |fb|
		fbMulEnvFlag = fb;
		s.sendMsg('n_set', instGroup, 'fbMulEnvFlag', fbMulEnvFlag);
	}
	setFreq2EnvFlag { |frq|
		freq2EnvFlag = frq;
		s.sendMsg('n_set', instGroup, 'freq2EnvFlag', freq2EnvFlag);
	}
	setFM2EnvFlag { |fm|
		fmEnvFlag = fm;
		s.sendMsg('n_set', instGroup, 'fmEnvFlag', fmEnvFlag);
	}
	setEnvScale { |scale|
		envScale = scale;
		s.sendMsg('n_set', instGroup, 'envScale', envScale);	
	}
	setEnvelope { |env|
		att = (env[0][1] - env[0][0]);
		dec = (env[0][2] - env[0][1]);
		sus = (env[0][3] - env[0][2]);
		rel = (env[0][4] - env[0][3]);
		peakA = env[1][1];
		peakB = env[1][2];
		peakC = env[1][3];
		s.sendMsg('n_set', instGroup, 'att', att, 'dec', dec, 'sus', sus, 'rel', rel, 'peakA', peakA, 'peakB', peakB, 'peakC', peakC);
	}
	setPitchBendFlag { |flag|
		this.addModulator(flag, 'bend', 'pitchBend');	}
	addMixerChannel {
		parent.mixer.addMonoChannel("WavetableSynth", 0);
		outBus = parent.mixer.channels["WavetableSynth"].inBus;
	}
	addActiveNote { |noteNum,id|
		var lastNote;
		if(activeNotes[noteNum].notNil){
			lastNote = activeNotes[noteNum];
			activeNotes[noteNum] = lastNote ++ id;
		}{
			activeNotes = activeNotes.add(noteNum -> id.asArray);
		};
	}
	
	noteOn { |src,chan,num,vel|
		this.doNoteOn(src,chan,num,vel);
		if(this.looper.notNil){
			this.looper.addEvent([0,src,chan,num,vel]);
		};
	}
	
	doNoteOn { |src,chan,num,vel|
		var pitch;
		pitch = num.degreeToKey(tunings[tuning][0], tunings[tuning][1]).midicps;
		this.addActiveNote(num, s.nextNodeID);
		noteOnCommand.value(num, vel, pitch);
		if(midiThru){
			midiOut.noteOn(1, num, vel);
		};
	}
	
	noteOff { |src,chan,num,vel|
		this.doNoteOff(src,chan,num,vel);
		if(this.looper.notNil){
			this.looper.addEvent([1,src,chan,num,0]);
		};

	}
	
	doNoteOff { |src,chan,num,vel|
		var lastNote;
		if(activeNotes[num].notNil){
			lastNote = activeNotes[num];
			noteOffCommand.value(lastNote[0]);
			if(lastNote.size == 1){
				activeNotes.removeAt(num);
			}{
				activeNotes[num].removeAt(0);
			};
			if(midiThru){
				midiOut.noteOff(1, num, vel);
			};
		};
	}
	
	bend { |src,chan,val|
		this.doBend(src,chan,val);
		if(this.looper.notNil){
			this.looper.addEvent([4,src,chan,val]);
		};

	}
	
	doBend { |src,chan,val|
		this.handleMIDI(modulatorSources['bend'], val / 16384);
	}
		
	afterTouch { |src,chan,val|
		this.doAfterTouch(src,chan,val);
		if(this.looper.notNil){
			this.looper.addEvent([3,src,chan,val]);
		};
	}
	
	doAfterTouch { |src,chan,val|
		this.handleMIDI(modulatorSources['aftertouch'], val / 127);
	}
	
	cc { |src,chan,num,val|
		this.doCC(src,chan,num,val);
		if(this.looper.notNil){
			this.looperHandleCC(src,chan,num,val);
		};
	}
	
	doCC { |src,chan,num,val|
		if(midiCCSources[num].notNil){
			this.handleMIDI(modulatorSources[midiCCSources[num]], val / 127);
		};
	}
	
	handleMIDI { |controls,value|
		if(controls.size > 0){
			controls.do{ |obj,ind|
				obj.switch(
					'xFade', {
						this.setXFade(xfadeSpec.map(value));
						//defer{ xfadeKnob.zeroOneValue = value };
					},
					'fbMul', {
						this.setFBMul(fbMulSpec.map(value));
						//defer{ fbMulKnob.zeroOneValue = value };
					},
					'freq2', {
						this.setFreq2(freq2Spec.map(value));
						//defer{ freq2Knob.zeroOneValue = value };
					},
					'fm2', {
						this.setFM2(fm2Spec.map(value));
						//defer{ fm2Knob.zeroOneValue = value };
					},
					'pitchBend', {
						this.setPitchBend(value);
					}
				);
			}
		};
	}

	initGUI {
		win = GUI.window.new("Dual Wavetable Synth", Rect.new(50,300, 400, 340)).front;
		win.view.decorator = FlowLayout(win.view.bounds);
		
		modeRow = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 20))
			.background_(Color.blue(0.1, alpha:0.2));
		GUI.staticText.new(modeRow, Rect.new(0, 0, win.view.bounds.width * 0.24, 0))
			.string_("tuning:");
		modeMenu = GUI.popUpMenu.new(modeRow, Rect.new(0, 0, win.view.bounds.width * 0.74, 0))
			.items_(tunings.keys.asArray)
			.action_({ |obj| this.setTuning(obj.item); });
		modeMenu.value_(modeMenu.items.indexOf('centaur'));
		
		presetRow = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 20))
			.background_(Color.blue(0.1, alpha:0.2));
		saveButton = GUI.button.new(presetRow, Rect.new(0, 0, 75, 0))
			.states_([["save", Color.black, Color.green]])
			.action_({ |obj| this.setSave(presetNameField.string); });
		presetNameField = GUI.textField.new(presetRow, Rect.new(0, 0, 75, 0))
		    .string_("<>")
			.action_({ |obj| this.savePreset(obj.string); });
		presetMenu = GUI.popUpMenu.new(presetRow, Rect.new(0, 0, 230, 0))
			.items_((saveRoot ++ sep ++ "*").pathMatch.collect{ |obj,ind| obj.split($/).last; })
			.action_({ |obj| this.loadPreset(obj.item) });
			
		// controls row 1
		partialRow1 = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 75))
			.background_(Color.blue(0.1, alpha:0.2));
		GUI.staticText.new(partialRow1, Rect.new(0, 0, 11, 0))
			.string_("A")
			.align_('center')
			.background_(Color.black.alpha_(0.9))
			.stringColor_(Color.white);
		partialAAmps = GUI.multiSliderView.new(partialRow1, Rect.new(0, 0, 85, 0))
			.fillColor_(Color.blue)
			.strokeColor_(Color.blue)
			.indexThumbSize_(3.4)
			.background_(Color.black.alpha_(0.9))
			.valueThumbSize_(3.4)
			.value_([1,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0])
			.isFilled_(true)
			.action_({ |obj| this.generatePartials(partialAFreqs.value, obj.value, bufferA) });
		partialAFreqs = GUI.multiSliderView.new(partialRow1, Rect.new(0, 0, 85, 0))
			.fillColor_(Color.red)
			.strokeColor_(Color.red)
			.indexThumbSize_(3.4)
			.background_(Color.black.alpha_(0.9))
			.valueThumbSize_(3.4)
			.value_(Array.fill(19, { 0.5 }))
			.action_({ |obj| this.generatePartials(obj.value, partialAAmps.value, bufferA) });
		GUI.staticText.new(partialRow1, Rect.new(0, 0, 11, 0))
			.string_("B")
			.align_('center')
			.background_(Color.black.alpha_(0.9))
			.stringColor_(Color.white);
		partialBAmps = GUI.multiSliderView.new(partialRow1, Rect.new(0, 0, 85, 0))
			.fillColor_(Color.blue)
			.strokeColor_(Color.blue)
			.background_(Color.black.alpha_(0.9))
			.indexThumbSize_(3.4)
			.valueThumbSize_(3.4)
			.value_([1,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0])
			.isFilled_(true)
			.action_({ |obj| this.generatePartials(partialBFreqs.value, obj.value, bufferB) });
		partialBFreqs = GUI.multiSliderView.new(partialRow1, Rect.new(0, 0, 85, 0))
			.fillColor_(Color.red)
			.strokeColor_(Color.red)
			.background_(Color.black.alpha_(0.9))
			.indexThumbSize_(3.4)
			.valueThumbSize_(3.4)
			.value_(Array.fill(19, { 0.5 }))
			.action_({ |obj| this.generatePartials(obj.value, partialBAmps.value, bufferB) });

		// waveform edit
		waveformDraw = GUI.multiSliderView.new(win, Rect.new(0, 0, win.view.bounds.width * 0.85, 85))
			.value_(Array.fill(256, {0.5}))
			.fillColor_(Color.white)
			.strokeColor_(Color.white)
			.background_(Color.black.alpha_(0.9))
			.indexThumbSize_(0.32)
			.valueThumbSize_(3)
			.isFilled_(false)
			.canFocus_(false)
			.action_({ |obj| 
				if( targetAButton.value == 1 ){
					this.drawWaveform(obj.value, obj.index, bufferA);
				};
				if( targetBButton.value == 1 ){
					this.drawWaveform(obj.value, obj.index, bufferB);
				};
			});
		targetColumn = GUI.vLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width * 0.12, 85))
			.background_(Color.blue(0.1, alpha:0.2));
		targetAButton = GUI.button.new(targetColumn, Rect.new(0, 0, 0, targetColumn.bounds.height * 0.42))
			.states_([["A", Color.black, Color.clear],["A", Color.red, Color.yellow]]);
		targetBButton = GUI.button.new(targetColumn, Rect.new(0, 0, 0, targetColumn.bounds.height * 0.42))
			.states_([["B", Color.black, Color.clear],["B", Color.red, Color.yellow]]);
		
		// bottom aux controls
		pr2AuxControls = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 25))
			.background_(Color.blue(0.1, alpha:0.2));
		xFadeMenu = GUI.popUpMenu.new(pr2AuxControls, Rect.new(0, 0, 37.5, 0))
			.items_(midiListMenu)
			.action_({ |obj| this.addModulator(obj.value, obj.item, 'xFade');});
		GUI.staticText.new(pr2AuxControls, Rect.new(0, 0, 37.5, 0));
		fbMulMenu = GUI.popUpMenu.new(pr2AuxControls, Rect.new(0, 0, 37.5, 0))
			.items_(midiListMenu)
			.action_({ |obj| this.addModulator(obj.value, obj.item, 'fbMul'); });
		freq2Menu = GUI.popUpMenu.new(pr2AuxControls, Rect.new(0, 0, 37.5, 0))
			.items_(midiListMenu)
			.action_({ |obj| this.addModulator(obj.value, obj.item, 'freq2'); });
		fm2Menu = GUI.popUpMenu.new(pr2AuxControls, Rect.new(0, 0, 37.5, 0))
			.items_(midiListMenu)
			.action_({ |obj| this.addModulator(obj.value, obj.item, 'fm2'); });
		syncModeMenu = GUI.popUpMenu.new(pr2AuxControls, Rect.new(0, 0, 110, 0))
			.items_(["no sync", "soft sync", "hard sync"])
			.action_({ |obj| this.setSyncMode(obj.value); });

		// bottom control row
		partialRow2 = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 75))
			.background_(Color.blue(0.1, alpha:0.2));
		xfadeKnob = EZJKnob.new(partialRow2, Rect.new(0, 0, 37.5, 73), "xfade")
			.knobColor_([Color.black, Color.green, Color.black, Color.green])
			.spec_(xfadeSpec)
			.knobAction_({ |obj| this.setXFade(obj.value); })
			.knobCentered_(true);
		fbLagKnob = EZJKnob.new(partialRow2, Rect.new(0, 0, 37.5, 73), "fbLag")
			.spec_(fbLagSpec)
			.value_(0.3)
			.knobColor_([Color.black, Color.green, Color.black, Color.green])
			.knobAction_({ |obj| this.setFBLag(obj.value); });
		fbMulKnob = EZJKnob.new(partialRow2, Rect.new(0, 0, 37.5, 73), "fbMul")
			//.spec_() // wha?
			.knobColor_([Color.black, Color.green, Color.black, Color.green])
			.knobAction_({ |obj| this.setFBMul(obj.value); });
		freq2Knob = EZJKnob.new(partialRow2, Rect.new(0, 0, 37.5, 73), "freq2")
			.spec_(freq2Spec)
			.knobColor_([Color.black, Color.green, Color.black, Color.green])
			.knobAction_({ |obj| this.setFreq2(obj.value); })
			.knobCentered_(true);
		fm2Knob = EZJKnob.new(partialRow2, Rect.new(0, 0, 37.5, 73), "fm")
			.spec_(fm2Spec)
			.knobColor_([Color.black, Color.green, Color.black, Color.green])
			.knobAction_({ |obj| this.setFM2(obj.value); });
		envelopeView = GUI.envelopeView.new(partialRow2, Rect.new(0, 0, 150, 0))
			.value_([[0.0, 0.05, 0.15, 0.8, 1.0], [0.0, 0.99, 0.5, 0.65, 0.0]])
			.thumbSize_(5)
			.fillColor_(Color.green)
			.strokeColor_(Color.green)
			.background_(Color.black.alpha_(0.9))
			.drawLines_(true)
			.setEditable(0, false)
			.setEditable(4, false)
			.action_({ |obj| this.setEnvelope(obj.value); });

		// bottom env buttons
		pr2EnvRow = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 25))
			.background_(Color.blue(0.1, alpha:0.2));
		bendButton = GUI.button.new(pr2EnvRow, Rect.new(0, 0, 62, 0))
			.states_([["pitch bend", Color.black, Color.clear],["pitch bend", Color.red, Color.yellow]])
			.action_({ |obj| this.setPitchBendFlag(obj.value); });
		GUI.staticText.new(pr2EnvRow, Rect.new(0, 0, 6, 0));
		fbMulEnvButton = GUI.button.new(pr2EnvRow, Rect.new(0, 0, 34, 0))
			.states_([["env", Color.black, Color.clear],["env", Color.red, Color.yellow]])
			.action_({|obj| this.setFBMulEnvFlag(obj.value) });
		freq2EnvButton = GUI.button.new(pr2EnvRow, Rect.new(0, 0, 34, 0))
			.states_([["env", Color.black, Color.clear],["env", Color.red, Color.yellow]])
			.action_({|obj| this.setFreq2EnvFlag(obj.value) });
		fm2EnvButton = GUI.button.new(pr2EnvRow, Rect.new(0, 0, 34, 0))
			.states_([["env", Color.black, Color.clear],["env", Color.red, Color.yellow]])
			.action_({|obj| this.setFM2EnvFlag(obj.value) });
		envScaleSpec = [0.2, 10, 2.2].asSpec;
		envScaleSlider = GUI.slider.new(pr2EnvRow, Rect.new(0, 0, 150, 0))
			.background_(Color.black.alpha_(0.9))
			.value_(envScaleSpec.unmap(1))
			.action_({ |obj| this.setEnvScale(envScaleSpec.map(obj.value)); });
	}
	
	addModulator { |value,sourceName,effectName|
		if(currentModulators[effectName].notNil){
			modulatorSources[currentModulators[effectName]]
				.removeAt(modulatorSources[currentModulators[effectName]].indexOf(effectName));
		};
		if(value > 0){
			modulatorSources[sourceName] = modulatorSources[sourceName].add(effectName);
			currentModulators[effectName] = sourceName;
		}{
			currentModulators[effectName] = nil;
		};
	}
	octaveToRatio { |oct|
		var ret;
		if(oct > 0){
			ret = oct + 1;
		}{
			ret = (oct - 1).abs.reciprocal;
		};
		^ret;
	}

}

WavetableSynthFilter : WavetableSynth {
	var cutoff=0, cutoffMod=0, cutoffFlag=0, cutoffModFlag=0, resonance=1, modSource=0, cutoffKnob, cutoffModKnob, filterMidiRow, filterControlRow, filterEnvRow, cutoffMenu, cutoffModMenu, cutoffEnvButton, cutoffModEnvButton, filterTypeMenu, rezKnob, cutoffModSourceButton, currentFilter,
	    cutoffSpec, cutoffModSpec;
	*new { |par,midi|
		^super.new(par, midi).init_wavetablesynthfilter;
	}
	
	init_wavetablesynthfilter {
		"WavetableSynthFilter initializing".postln;
		saveRoot = Platform.userAppSupportDir ++ sep ++ "Presets" ++ sep ++ "WavetableSynthFilter";
		noteOnCommand = { |num,vel,pitch|
			s.sendMsg('s_new', 's_dualWavetableRLPF', activeNotes[num].last, 0, instGroup,
				'outBus', outBus, 'freq1', pitch, 'lev', (vel / 127).pow(2.2),
				'peakA', peakA, 'peakB', peakB, 'peakC', peakC,  'bufferA', bufferA, 'bufferB', bufferB,
				'att', att, 'dec', dec, 'sus', sus, 'rel', rel, 
				'trigMode', trigMode, 'xfade', xfade, 
				'fbLag', fbLag, 'fbMul', fbMul, 'freq2', freq2, 'fmAmt', fmAmt, 
				'fbMulEnvFlag', fbMulEnvFlag, 'freq2EnvFlag', freq2EnvFlag, 'fmEnvFlag', fmEnvFlag, 
				'envScale', envScale, 'bend', pitchBend,
				'cutoff', cutoff, 'cutoffMod', cutoffMod, 'cutoffFlag', cutoffFlag, 'cutoffModFlag', cutoffModFlag, 
				'resonance', resonance, 'modSource', modSource);
			s.sendMsg('n_set', activeNotes[num].last, 'gate', 1);

		};

		cutoffSpec = [-12,12].asSpec;
		cutoffModSpec = [0, 8].asSpec;

		this.addGUI;
		this.setFilterType("MoogVCF");
		//filterTypeMenu.value = filterTypeMenu.items.indexOf(currentFilter);
	}

	setFilterType { |sel|
		currentFilter = sel;
		rezKnob.spec = parent.filterSpecs[currentFilter];
		this.loadSynthDef(parent.filterUGens[currentFilter]);
	}
	
	getParams  {
		("getting " ++ currentFilter ++ " to save").postln;
		^super.getParams ++ [cutoff, cutoffMod, cutoffFlag, cutoffModFlag, resonance, modSource, cutoffMenu.value, cutoffModSourceButton.value, cutoffKnob.value, cutoffModMenu.value, cutoffModKnob.value, rezKnob.value, cutoffEnvButton.value, cutoffModEnvButton.value, currentFilter, filterTypeMenu.value]; // this doesn't fix the problem
	}
	
	setParams { |values|
		super.setParams(values);
		cutoff = values[44];
		cutoffMod = values[45];
		cutoffFlag = values[46];
		cutoffModFlag = values[47];
		resonance = values[48];
		modSource = values[49];
		cutoffMenu.valueAction = values[50];
		cutoffModSourceButton = values[51];
		cutoffKnob.value = values[52];
		cutoffModMenu.valueAction = values[53];
		cutoffModKnob.value = values[54];
		rezKnob.value = values[55];
		cutoffEnvButton.value = values[56];
		cutoffModEnvButton.value = values[57];
		this.setFilterType(values[58].asString); 
		filterTypeMenu.value = values[59]; // probably can't rely on the menu order being the same each time
		
	}
	
	
	setCutoff { |val|
		cutoff = this.octaveToRatio(val);
		s.sendMsg('n_set', instGroup, 'cutoff', cutoff);
	}
	
	setCutoffMod { |val|
		cutoffMod = val;
		s.sendMsg('n_set', instGroup, 'cutoffMod', cutoffMod);
	}
	
	setCutoffFlag { |val|
		cutoffFlag = val;
		s.sendMsg('n_set', instGroup, 'cutoffFlag', cutoffFlag);
	}
	
	setCutoffModFlag { |val|
		cutoffModFlag = val;
		s.sendMsg('n_set', instGroup, 'cutoffModFlag', cutoffModFlag);
	}
	
	setResonance { |val|
		resonance = val;
		s.sendMsg('n_set', instGroup, 'resonance', resonance);
	}
	
	setModSource { |val|
		modSource = val;
		s.sendMsg('n_set', instGroup, 'modSource', modSource);
	}
	
	handleMIDI { |controls,value|
		super.handleMIDI(controls, value);
		if(controls.size > 0){
			controls.do{ |obj,ind|
				obj.switch(
					'cutoff', {
						this.setCutoff(cutoffSpec.map(value));
						//defer{ cutoffKnob.zeroOneValue = value; };
					},
					'cutoffMod', {
						this.setCutoffMod(cutoffModSpec.map(value));
						//defer{ cutoffModKnob.zeroOneValue = value; };
					}
				);
			}
		};
	}
	
	addGUI {
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
	}

	loadSynthDef { |filter|
		filter = filter ? parent.filterUGens["MoogFF"];
		SynthDef.new("s_dualWavetableRLPF", { 
			|gate, outBus=20, 
				curve=(-1.6), lev=1,
				peakA=0.7, peakB=0.5, peakC=0.6, 
				att=0.1, dec=0.2, sus=0.3, rel=0.2, 
				fbMul=0, fbLag=1, fmAmt=0,
				freq1=440, freq2=0, trigMode=0, bend=1, xfade=0, resonance=1,
				bufferA=70, bufferB=71, 
				fmEnvFlag=0, freq2EnvFlag=0, fbMulEnvFlag=0, envScale=1,
				cutoff=0, cutoffMod=0, cutoffFlag=0, cutoffModFlag=0, modSource=0|
			var aTabReadA, aTabReadB, aTabPhase1, aTabPhase2, aEnv, aInFreq, aSig, aFreq1, aFreq2, aTrig, aConstant1, aInvSync, lSyncCoefs, aSyncCoef, asFreq2Env, kFreq1, kFreq2, aFM2, asInEnv, aFilt, aLPFreq, asLPFModSources, aLPFreqMod, asCutoff, asCutoffFreqEnv, asCutoffModEnv;
			 
			aEnv = EnvGen.ar(Env.new([0, peakA, peakB, peakC, 0], [att, sus, dec, rel], curve, 3), gate, lev, 0, envScale, 2);
			aConstant1 = DC.ar(1);
		
			asInEnv = Select.ar(fbMulEnvFlag, [aConstant1, aEnv]);
			aInFreq = Lag2.ar(InFeedback.ar(outBus) * fbMul * asInEnv, fbLag);
			
			kFreq1 = freq1 * bend;
			aFreq1 = kFreq1 + aInFreq;
			kFreq2 = freq2 * kFreq1;
			asFreq2Env = Select.ar(freq2EnvFlag, [aConstant1, aEnv]);
			aFreq2 = (kFreq2 * asFreq2Env) +  kFreq1;
		 
			aTabPhase1 = Phasor.ar(0, aFreq1 * 0.023219954648526, 0, 1024);
			aTabReadA = BufRd.ar(1, bufferA, aTabPhase1);
		
			aTrig = (aTabPhase1 * -1  + 512) * trigMode;
			aFM2 = aTabReadA * fmAmt * freq1;
			aTabPhase2 = Phasor.ar(aTrig, (aFreq2 + aFM2) * 0.023219954648526, 0, 1024);
		
			aInvSync = ((aTabPhase1 / 1024) - 1).abs;
			lSyncCoefs = [aConstant1, aInvSync, aConstant1];
			aSyncCoef = Select.ar(trigMode, lSyncCoefs);
		
			aTabReadB = BufRd.ar(1, bufferB, aTabPhase2) * aSyncCoef;
		   
		 	aSig = XFade2.ar(aTabReadA, aTabReadB, xfade);
		 	
		 	asCutoffFreqEnv = Select.ar(cutoffFlag, [aConstant1, aEnv]);
		 	asCutoffModEnv = Select.ar(cutoffModFlag, [aConstant1, aEnv]);
			asLPFModSources = Select.ar(modSource, [aTabReadA, aTabReadB]);
			
			aLPFreqMod = freq1 * asLPFModSources * cutoffMod * asCutoffModEnv;
			aLPFreq = (freq1 + (cutoff * freq1 * asCutoffFreqEnv)) + aLPFreqMod;
			
			aFilt = SynthDef.wrap(filter, [0, 0, 0], [aSig.softclip, aLPFreq, resonance]);
		 
			Out.ar(outBus, aFilt * aEnv);
		}, [nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, nil, 1, nil, 1, 1, 1, nil, 1, 1, 1, nil, nil, nil, nil, nil, nil, 1, 1]).load(s);
		
		/*		SynthDef.new("wavetableSynthLFO", {
			|freq, waveformType, outBus|
			var aCub, aTri, aSig;
			
			aCub = LFCub.ar(0.2, 0, 1);			
			aCub = LFTri.ar(0.2, 0, 1);
			
			aSig = Select.ar(waveformType, [aCub, aTri]);

			Out.ar(outBus, aSig);
			
		}).load(s);
		*/
	
	}
	
}
                    
