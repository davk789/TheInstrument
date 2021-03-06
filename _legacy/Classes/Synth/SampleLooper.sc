
Sampler { // container for one or more SampleLoopers
	var parent, <>win, activeMidiChannels, <>channels, 
		<outBus, recorderID, <looperCommands, keyActions,
		controlView, midiButtons, sampleScrollView, samplerChannelsView, <controlBackgroundColor,
		addEmptyBufferBox, addEmptyBufferButton, addFileButton, <>buffers, <>bufferMarkers, 
		s, saveRoot;
	*new { |env, loopers|
		^super.new.init_sampler(env, loopers);
	}
	
	init_sampler { |env, loopers=1|
		var sep;
		s = Server.default;
		parent = env;
		channels = Array.new;
		midiButtons = Array.new;
		controlBackgroundColor = Color.blue.alpha_(0.2);
		activeMidiChannels = Array.new;
		buffers = Array.new;
		bufferMarkers = Array.new;
		recorderID = "Sampler";
		sep = Platform.pathSeparator;
		saveRoot = Platform.userAppSupportDir ++ sep ++ "Presets" ++ sep ++ "Sampler";
		looperCommands = [ // i wish this was an enum
			'loopRange',
			'loopRangeRelease',
			'play',
			'pause',
			'stop',
			'back',
			'forward',
			'modLag',
			'modLevel',
			'speed',
			'gain'
		];
		keyActions = Dictionary.new;
		
		this.initGUI;
		loopers.do{ |ind|
			var keyNum;
			this.addChannel(ind);
			if(ind < 10){
				keyNum = (ind + 1) % 10;
				keyActions = keyActions.add( 
					("\$" ++ keyNum).interpret -> { 
						ind.postln;
						midiButtons[ind].valueAction_(activeMidiChannels.includes(ind).not.toInt);
					} 
				);
			};
		};
		this.addMixerChannel;
		this.useKeyboard;
		this.initLooper;
	}
	
	useKeyboard {
		GUI.view.globalKeyDownAction = { |obj,char,mod,unic,keyc| keyActions[char].value; };
	}
	
	addChannel { |num|
		channels = channels.add(SampleLooper.new(parent, this, channels.size));
		//win.bounds = Rect.new(win.bounds.left, win.bounds.top, 830, channels.size * 270);
		samplerChannelsView.bounds = Rect.new(
			samplerChannelsView.bounds.left, 
			samplerChannelsView.bounds.top,
			samplerChannelsView.bounds.width,
			samplerChannelsView.bounds.height + channels.last.viewBounds.height
		);
		channels.last.makeGUI(samplerChannelsView);
		midiButtons = midiButtons.add(
			GUI.button.new(controlView, Rect.new(0, 0, controlView.bounds.height, 0))
				.states_([
					[(num + 1).asString, Color.yellow, Color.black],
					[(num + 1).asString, Color.black, Color.yellow]
				])
				.action_({ |obj| this.setActiveMidiChannel(obj.value.toBool, num); })
		);
	}
	
	setActiveMidiChannel { |sel,chan|
		if(sel){
			activeMidiChannels = activeMidiChannels.add(chan);
		}{
			activeMidiChannels.remove(chan);
		};
	}
	
	addMixerChannel {
		parent.mixer.addStereoChannel("Sampler", 0);
		outBus = parent.mixer.channels["Sampler"].inBus;
		channels.do{ |obj,ind|
			obj.outBus = outBus;
		}
	}
	
	initLooper {
		parent.eventLooper.addChannel(0, recorderID);
		parent.eventLooper.channels[recorderID].action = { |values,index|
			switch(values[0],
				looperCommands.indexOf('loopRange'), {
					Platform.case(
						'linux', {
							channels[values[1]].setLoopPointParams(values[2], values[3]);
						},
						'osx', {
							defer{ channels[values[1]].setLoopRange(values[2], values[3]); };
						}
					);

					// SampleView.action
				},
				looperCommands.indexOf('loopRangeRelease'), {
					channels[values[1]].server.sendMsg('n_set', channels[values[1]].playerNodeNum, 'trig', 0);
				},
				looperCommands.indexOf('play'), {
					channels[values[1]].play; 
					defer{ channels[values[1]].playButton.value = 1; };
				},
				looperCommands.indexOf('pause'), {
					channels[values[1]].pause(values[2].toBool);
					defer{ channels[values[1]].pauseButton.value = channels[values[1]].pauseButton.value + 1 % 2; };
				},
				looperCommands.indexOf('stop'), {
					defer{ channels[values[1]].stop; };
				},
				looperCommands.indexOf('back'), {
					defer{ channels[values[1]].back; };
				},
				looperCommands.indexOf('forward'), {
					defer{ channels[values[1]].forward; };
				},
				looperCommands.indexOf('modLag'), {
					channels[values[1]].setModLag(values[2]);
				},
				looperCommands.indexOf('modLevel'), {
					channels[values[1]].setModLevel(values[2]);
				},
				looperCommands.indexOf('speed'), {
					channels[values[1]].setSpeed(values[2]);
				},
				looperCommands.indexOf('gain'), {
					channels[values[1]].setGain(values[2]);
				}
			);
		};
	}
	
	looper { // shortcut for local usage
		^parent.eventLooper.channels[recorderID];
	}
	
	initGUI {
		win = GUI.window.new("Sample Loopers", Rect.new(500.rand, 500.rand, 820, 530)).front;
		win.view.decorator = FlowLayout(win.view.bounds);
		
		/// row 1
		
		controlView = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 25))
			.background_(Color.black)
			.resize_(2);
			
		addFileButton = GUI.button.new(controlView, Rect.new(0, 0, 75, 0))
			.states_([["add file(s)", Color.white, controlBackgroundColor]])
		    .font_(parent.controlFont)
		    .action_({ |obj| this.addBufferFromFile; });
		
		addEmptyBufferBox = GUI.numberBox.new(controlView, Rect.new(0, 0, 30, 0))
		    .font_(parent.controlFont)
		    .value_(20)
		    .action_({ |obj| this.addEmptyBuffer(obj.string.interpret) });
		
		addEmptyBufferButton = GUI.button.new(controlView, Rect.new(0, 0, 90, 0))
			.states_([["add empty buffer", Color.white, controlBackgroundColor]])
		    .font_(parent.controlFont)
		    .action_({ |obj| this.addEmptyBuffer(addEmptyBufferBox.string.interpret) });

			
		// the buttons get added last
		GUI.staticText.new(controlView, Rect.new(0, 0, 100, 0))
			.stringColor_(Color.white)
			.align_('right')
			.string_("midi active:")
			.font_(parent.controlFont);
		
 		/// area 2
 		
		sampleScrollView = GUI.scrollView.new(win, Rect.new(0, 0, win.bounds.width - 5, win.bounds.height - 25))
			.resize_(5);
		samplerChannelsView = GUI.vLayoutView.new(sampleScrollView, Rect.new(0, 0, sampleScrollView.bounds.width, 5));
	}
	
	addBufferFromFile {
		Dialog.getPaths({ |paths|
			paths.do{ |obj,ind|
				buffers = buffers.add(Buffer.read(s, obj));
				bufferMarkers = bufferMarkers.add(Array.new);
				if(ind == paths.lastIndex){
					// just updating the GUI "later"
					// may fail with large buffers
					AppClock.sched(1, {this.updateBufferMenus; nil; });
				};
			};
		});
	}
	
	addEmptyBuffer { |len|
		var length;
		// can only add empty mono buffers for now.
		// multi-channel support should come later.
		length = len ? addEmptyBufferBox.value;
		buffers = buffers.add(Buffer.alloc(s, length * s.sampleRate, 1));
		bufferMarkers = bufferMarkers.add(Array.new);
		AppClock.sched(1, {this.updateBufferMenus; nil;});
	}
	
	cc { |src,chan,num,val|
		channels.do{ |obj,ind|
			if(activeMidiChannels.includes(ind)){
				obj.cc(src,chan,num,val);
			};
		};
	}
	
	updateBufferMenus {
		channels.do{ |obj,ind|
			obj.updateBufferMenu;
		};
	}
	
}

SampleLooper : InstrumentVoice {
	classvar <groupNum=55;
	var parent, s, <playerNodeNum, <recorderNodeNum, playerParams, recorderParams, 
		paused=false, synthOutputs, synthInputs, activeBufferIndex=0, activeMarkerIndex=0, 
		currentBufferArray, currBufDisplayStart, currBufDisplayEnd, 
		waveformVZoomSpec, waveformDisplayResolution=557, isRecording=false, 
		loopMarkers, isPlaying=false, isRecording=false, 
		looperCommands,
		looper, <>sampler, numChannels, <viewBounds, jumpToMarker=false, channelID,
		playerMap, recorderMap,
		saveRoot;
	// GUI objects
	var topView, bufferRow, presetLoadMenu, presetSaveField, presetSaveButton, waveformControlView, <>waveformMarkerBar, waveformMarkerClearButton, waveformView, waveformDisplay, waveformViewVZoomView, waveformViewVZoom, waveformViewZoom, controlView, recordButton, backButton, <playButton, forwardButton, pauseButton, stopButton, playbackSpeedKnob, clearBufferButton, bufferSelectMenu, jumpToMarkerButton, modBusMenu, modLevelKnob, modLagKnob, speedKnob, gainKnob, panKnob, inputSourceMenu, inputLevelKnob, preLevelKnob, syncOffsetKnob, recordModeButton, recordOffsetKnob;

	
	*new { |par,samp,chan|
		^super.new.init_samplelooper(par,samp,chan);
	}
	
	init_samplelooper { |par,samp,chan=0|
		var sep;
		parent  = par;
		sampler = samp;
		channelID = chan;
		s       = Server.default;
		playerNodeNum = s.nextNodeID;
		sep = Platform.pathSeparator;
		saveRoot = Platform.userAppSupportDir ++ sep ++ "Presets" ++ sep ++ "SampleLooper";
		recorderNodeNum = s.nextNodeID;
		waveformVZoomSpec = [1,10].asSpec;
		viewBounds = Rect.new(0, 0, 812, 245);
		playerParams = Dictionary[
			'bufnum'       -> -1, 
			'speed'        -> 1, 
			'start'        -> 0, 
			'end'          -> 1, 
			'outBus'       -> 0, 
			'trig'         -> 0, // where is this used?
			'modBus'       -> 20, 
			'modLag'       -> 0.2, 
			'modLev'       -> 0,
			'inBus'        -> 1,
			'gain'		   -> 1,
			'pan'		   -> 0
		];
		recorderParams = Dictionary[
			'bufnum'       -> -1, 
			'trig'         -> 0, 
			'inBus'        -> 20,
			'stereo'       -> 0, // where is this used?
			'inLev'        -> 1,
			'preLev'       -> 0			
		];
		// waveformDisplayResolution = 8 - waveformView.bouds.width
		currentBufferArray = Array.fill(waveformDisplayResolution, { 0.5 });
		currBufDisplayStart = 0;
		currBufDisplayEnd = currentBufferArray.last;
		looperCommands = sampler.looperCommands;
		
		loopMarkers = Array.new;
		ccFunction = { |src,chan,num,val|
			[src,chan,num,val].postln;
			defer{
				num.switch(
					20, {
						postln("what should I be user for??");
					},
					21, {
						this.back;
						this.addLooperEvent('back');
					},
					22, {
						this.forward;
						this.addLooperEvent('forward');
					},
					23, {
						this.stop;
						this.addLooperEvent('stop');
					},
					24, {
						this.play;
						playButton.value_(1);
						this.addLooperEvent('play');
					},
					25, {
						this.record(isRecording.not); // toggles isRecording in this function
						// record function should not automate for now
						recordButton.value_(isRecording.toInt); 
					}
				);
			};
		};
		synthOutputs = Dictionary[
			1 -> { |bus,sig,pan=0| Out.ar(bus, Pan2.ar(sig, pan)); },
			2 -> { |bus,sig,pan=0| Out.ar(bus, Balance2.ar(sig, pan)); }
		];
		synthInputs = Dictionary[
			1 -> { |bus, phase, bufnum, inLev, preLev, stereo, env| 
				[(InFeedback.ar(bus, 1) * inLev * env) + (BufRd.ar(1, bufnum, phase) * preLev)];
			},
			2 -> { |bus, phase, bufnum, inLev, preLev, stereo, env|
				[
					(InFeedback.ar(bus, 1) * inLev * env) + (BufRd.ar(1, bufnum, phase) * preLev), 
					(InFeedback.ar(bus + stereo, 1) * inLev * env) + (BufRd.ar(1, bufnum, phase) * preLev)
				]
			}
		];
		
		
		s.sendMsg('g_new', groupNum, 0, 1);


	}
	
	server {
		^s;
	}
	
	addLooperEvent { |command...args|
	   	if(sampler.looper.notNil){
	   		sampler.looper.addEvent([looperCommands.indexOf(command), channelID] ++ args);
	   	};
	}
	
	outBus {
		^playerParams['outBus'];
	}
	
	outBus_ { |val|
		playerParams['outBus'] = val;
	}
		
	record { |val|
		isRecording = val ? true;
		if(isRecording){
			s.listSendMsg(['s_new', 'SampleLooperRecorder', recorderNodeNum, 1, groupNum] ++ recorderParams.getPairs);
			s.sendMsg('n_set', recorderNodeNum, 'trig', 1);
		}{
			s.sendMsg('n_set', recorderNodeNum, 'trig', 0);
			waveformView.setBuffer(sampler.buffers[activeBufferIndex]);
		};
	}

	back {
		var lo,hi, range;
		if(waveformMarkerBar.value.size > 0){
			if(waveformMarkerBar.highlightRange.size > 0){
				if(waveformMarkerBar.highlightRange['low'] <= -1){
					range = waveformMarkerBar.highlightRange['high'] - waveformMarkerBar.highlightRange['low'];
					lo = waveformMarkerBar.value.size - range;
					hi = waveformMarkerBar.value.lastIndex + 1;
				}{
					lo = waveformMarkerBar.highlightRange['low'] - 1;
					hi = waveformMarkerBar.highlightRange['high'] - 1;
				};
			}{
				lo = waveformMarkerBar.value.lastIndex;
				hi = waveformMarkerBar.value.size;
			};
			waveformMarkerBar.setHighlightRange(lo,hi);
			this.setLoopPointParams;
		};
	}

	play { |val|
		isPlaying = val ? true;
		if(isPlaying){
			s.listSendMsg(['s_new', 'SampleLooperPlayer', playerNodeNum, 0, groupNum] ++ playerParams.getPairs);
		}{
			playButton.value_(1);
		};
	}
	
	forward {
		var lo,hi;
		if(waveformMarkerBar.value.size > 0){
			if(waveformMarkerBar.highlightRange.size > 0){
				if(waveformMarkerBar.highlightRange['high'] > waveformMarkerBar.value.lastIndex){
					lo = -1;
					hi = waveformMarkerBar.highlightRange['high'] - waveformMarkerBar.highlightRange['low'] - 1;
				}{
					lo = waveformMarkerBar.highlightRange['low'] + 1;
					hi = waveformMarkerBar.highlightRange['high'] + 1;
				};
			}{
				lo = -1;
				hi = 0;
			};
			waveformMarkerBar.setHighlightRange(lo,hi);
			this.setLoopPointParams;
		};
	}

	pause { |val|
		if(paused.not){
			s.sendMsg('n_set', playerNodeNum, 'speed', 0);
			paused = true;
		}{
			s.sendMsg('n_set', playerNodeNum, 'speed', playerParams['speed']);
			paused = false;
		};
	}
	
	stop {
		s.sendMsg('n_free', playerNodeNum);
		playButton.value_(0);
		if(recordButton.value == 1){
			recordButton.valueAction_(0);
		};
	}
	
	updateBufferMenu {
		var ret;
		sampler.buffers.do{ |obj,ind|
			if(obj.path.notNil){
				ret = ret.add(obj.path.basename);
			}{
				ret = ret.add(obj.bufnum.asString);
			}
		};
		bufferSelectMenu.items = ret;
	}

	setActiveBuffer { |sel|
		if(numChannels.isNil){
			this.loadSynthDef(1);
		};
		numChannels = sampler.buffers[activeBufferIndex].numChannels;

		activeBufferIndex = sel;
		recorderParams['bufnum'] = sampler.buffers[activeBufferIndex].bufnum;
		playerParams['bufnum'] = sampler.buffers[activeBufferIndex].bufnum;
//		postln("the bufnum is " ++ [playerParams['bufnum'], recorderParams['bufnum']]);
		if(sampler.buffers[activeBufferIndex].numChannels != numChannels){
			if(isPlaying || isRecording){
				this.stop;
			};
			this.loadSynthDef(numChannels);
		};
		s.sendMsg('n_set', playerNodeNum, 'bufnum', playerParams['bufnum']);
		s.sendMsg('n_set', recorderNodeNum, 'bufnum', recorderParams['bufnum']);
		this.setActiveMarkers(sel);
		waveformView.setBuffer(sampler.buffers[activeBufferIndex]);
		//this.drawWaveformView;
	}
	
	setActiveMarkers { |sel|
		sampler.bufferMarkers[activeMarkerIndex] = waveformMarkerBar.value;
		activeMarkerIndex = sel;
		waveformMarkerBar.value = sampler.bufferMarkers[activeMarkerIndex];
	}
	
	setWaveformVZoom { |amt|
		waveformView.vZoom = waveformVZoomSpec.map(amt);
	}
	
	setWaveformZoom { |start,range|
		waveformMarkerBar.zoom = [start, range];
		waveformView.zoom = [start, range];
	}
	
	clearActiveBuffer {
		sampler.buffers[activeBufferIndex].zero;
		waveformView.setBuffer(sampler.buffers[activeBufferIndex]);
	}

	setLoopRange { |start, end|

		if(waveformMarkerBar.value.size > 0){
			waveformMarkerBar.setHighlightCoords(start, end);
		};
		
		this.setLoopPointParams(start, end);
		
	}
	
	setLoopPointParams { |start,end|
		var highlightCoords, startPoint, paramStart, paramEnd;

		highlightCoords = waveformMarkerBar.getHighlightCoords;
		[start, end, highlightCoords].postln;
		
		if(start.notNil){
			paramStart = waveformMarkerBar.getNearestCoordBelow(start);
		}{
			paramStart = highlightCoords['low'];
		};

		if(end.notNil){
			paramEnd = waveformMarkerBar.getNearestCoordAbove(end);
		}{
			paramEnd = highlightCoords['high'];
		};
		
		playerParams['start'] = paramStart ? 0;
		playerParams['end'] = paramEnd ? 1;

		if(jumpToMarker){
			startPoint = playerParams['start'];
		}{
			startPoint = start;
		};
		postln("sending " ++ ['n_set', playerNodeNum, 'resetPos', startPoint, 'start', playerParams['start'], 'end', playerParams['end']]);
		s.sendMsg('n_set', playerNodeNum, 'resetPos', startPoint, 'start', playerParams['start'], 'end', playerParams['end']);
		s.sendMsg('n_set', playerNodeNum, 'trig', 1);
	
	}
	
	setJumpToMarker { |choice|
		jumpToMarker = choice;
	}
	
	setMarkers { |markers|
		loopMarkers = markers;
	}
		
	setModBus { |sel|
		playerParams['modBus'] = sel;
		s.sendMsg('n_set', playerNodeNum, 'modBus', playerParams['modBus']);
	}

	setModLevel { |val|
		playerParams['modLev'] = val;
		s.sendMsg('n_set', playerNodeNum, 'modLev', playerParams['modLev']);
	}
	
	setModLag { |val|
		playerParams['modLag'] = val;
		s.sendMsg('n_set', playerNodeNum, 'modLag', playerParams['modLag']);
	}

	setSpeed { |val|
		playerParams['speed'] = val;
		s.sendMsg('n_set', playerNodeNum, 'speed', playerParams['speed']);
	}

	setGain { |val|
		playerParams['gain'] = val;
		s.sendMsg('n_set', playerNodeNum, 'gain', playerParams['gain']);
	}
	
	setPan { |val| 
		playerParams['pan'] = val;
		s.sendMsg('n_set', playerNodeNum, 'pan', playerParams['pan']);
	}

	setInputSource { |sel|
		recorderParams['inBus'] = sel;
		s.sendMsg('n_set', recorderNodeNum, 'inBus', recorderParams['inBus']);
	}
	
	setInputLevel { |val| 
		recorderParams['inLev'] = val;
		s.sendMsg('n_set', recorderNodeNum, 'inLev', recorderParams['inLev']);
	}

	setPreLevel { |val| 
		recorderParams['preLev'] = val;
		s.sendMsg('n_set', recorderNodeNum, 'preLev', recorderParams['preLev']);
	}
	

	loadSynthDef { |numChan=1, trigBus=1000, startPointBus=1001, endPointBus=1002|
		postln("loading SynthDef");
		SynthDef.new( "SampleLooperPlayer", {
			arg bufnum, speed=1, start=0, end=1, outBus=0, trig=0, resetPos=0, 
				modBus=20, modLag=0.2, modLev=0,
				inBus=1, recordOffset=0.1, gain=1, pan=0;
			
			var outPhase, outSig, kNumFrames, modSig, kStart, kEnd, kResetPos, aTrig, kTrig, aSpeed;
	
			kNumFrames = BufFrames.kr(bufnum);
			kStart = kNumFrames * start;
			kEnd = kNumFrames * end;
			kResetPos = kNumFrames * resetPos;
			
			modSig = Lag.ar(InFeedback.ar(modBus) * modLev, modLag);
			aSpeed = Lag.ar(K2A.ar(speed), 1);
			outPhase = Phasor.ar(trig, aSpeed + modSig, kStart, kEnd, kResetPos);
			
			outSig = BufRd.ar(numChan, bufnum, outPhase);
			SynthDef.wrap(synthOutputs[numChan], nil, [outBus, outSig * gain, pan]);
			aTrig = ((outPhase - kStart) * -1) + ((kEnd - kStart) * 0.5);
			kTrig = A2K.kr(aTrig);

			Out.kr(trigBus, kTrig);
			Out.kr(startPointBus, kStart);
			Out.kr(endPointBus, kEnd);
			
		}).load(s);
		
		SynthDef.new("SampleLooperRecorder", { 
			arg bufnum, inBus=20, stereo=0, inNumChannels=1, inLev=1, preLev=0, recordMode=0, trig=0;
			
			var aRecordHead, inSig, skTrig, kNumFrames, kZero, aEnv;
			kZero = DC.kr(0);
			kNumFrames = BufFrames.kr(bufnum);
			
			skTrig = Select.kr(recordMode, [kZero, InTrig.kr(trigBus)]);

			aRecordHead = Phasor.ar(skTrig, BufRateScale.kr(bufnum), kZero, kNumFrames);
			aEnv = EnvGen.ar(Env.asr(0.1, 1, 0.1), trig, doneAction:2);
			BufWr.ar(SynthDef.wrap(synthInputs[numChan], nil, [inBus, aRecordHead, bufnum, inLev, preLev, stereo, aEnv]), bufnum, aRecordHead);

		}).load(s);
		
	}
	
	makeGUI { |container|
		var bgColor;
		
		bgColor = sampler.controlBackgroundColor;
		
		topView = GUI.compositeView.new(container, viewBounds);
		topView.decorator = FlowLayout(topView.bounds);
		// BUFFER ROW
		bufferRow = GUI.hLayoutView.new(topView, Rect.new(0, 0, topView.bounds.width, 25))
			.background_(Color.black);
		clearBufferButton = GUI.button.new(bufferRow, Rect.new(0, 0, 75, 0))
			.states_([["clear buffer", Color.white, bgColor]])
		    .font_(parent.controlFont)
		    .action_({ |obj| this.clearActiveBuffer; });
				
		bufferSelectMenu = GUI.popUpMenu.new(bufferRow, Rect.new(0, 0, 220, 0))
			.background_(bgColor)
			.stringColor_(Color.white)
		    .font_(parent.controlFont)
		    .action_({ |obj| this.setActiveBuffer(obj.value); });
		    
		jumpToMarkerButton = GUI.button.new(bufferRow, Rect.new(0, 0, 85, 0))
			.states_([
					["jump to marker", Color.white, bgColor],
					["jump to marker", Color.blue(0.3), Color.white]
			])
		    .font_(parent.controlFont)
		    .action_({ |obj| this.setJumpToMarker(obj.value.toBool) });
			
		// PRESETS

		GUI.staticText.new(bufferRow, Rect.new(0, 0, 25, 0))
			.string_("");
		presetSaveButton = GUI.button.new(bufferRow, Rect.new(0, 0, 45, 0))
		    .font_(parent.controlFont)
			.states_([["save", Color.white, Color.blue.alpha_(0.2)]])
		    .action_({ |obj| 
		    	this.savePreset(presetSaveField.string);
		    	presetSaveField.string = "";
		    });

		presetSaveField = GUI.textField.new(bufferRow, Rect.new(0, 0, 90, 0))
			.action_({ |obj| 
				this.savePreset(obj.string);
				obj.string = "";
			});

		presetLoadMenu = GUI.popUpMenu.new(bufferRow, Rect.new(0, 0, 145, 0))
			.items_(["this will have the presets listed", "some day"])
			.background_(bgColor)
		    .font_(parent.controlFont)
			.stringColor_(Color.white)
		    .action_({ |obj| this.loadPreset(obj.item); });

		// WAVEFORM CONTROL VIEW
		waveformControlView = GUI.compositeView.new(topView, Rect.new(0, 0, 600, 210))
			.background_(Color.black);

		waveformControlView.decorator_(FlowLayout(waveformControlView.bounds));
		
		waveformMarkerBar = MarkerBar.new(waveformControlView, Rect.new(0, 0, 565, 20))
			.markerColor_(Color.white)
			.background_(Color.blue.alpha_(0.2))
			.mouseDownAction_({ |obj,x,y,mod| this.setMarkers(obj.value); });

		waveformMarkerClearButton = GUI.button.new(waveformControlView, Rect.new(0, 0, 20, 20))
			.states_([["X", Color.black, Color.yellow]])
		    .font_(parent.controlFont)
		    .action_({ |obj| waveformMarkerBar.clear; });
		
		waveformView = SampleView.new(waveformControlView, Rect.new(0, 0, 565, 125))
			.mouseUpAction_({ |obj| 
				s.sendMsg('n_set', playerNodeNum, 'trig', 0);
				this.addLooperEvent('loopRangeRelease');
			})
		    .action_({  |obj|
		    	var start, end, bufferSize;
		    	bufferSize = obj.currentBuffer.numFrames;
		    	start = obj.sampleIndex / bufferSize;
		    	end = (obj.sampleSelectionSize / bufferSize) + start;
		    	this.setLoopRange(start,end);
		    	this.addLooperEvent('loopRange', start, end);
		    });
		    
		waveformViewVZoom = GUI.slider.new(waveformControlView, Rect.new(0, 0, 20, 125))
			.background_(bgColor)
			.knobColor_(HiliteGradient.new(bgColor, Color.white, \h, 64, 0.5))
			.action_({ |obj| this.setWaveformVZoom(obj.value); });
		
		waveformViewZoom = GUI.rangeSlider.new(waveformControlView, Rect.new(0, 0, 565, 20))
			.knobColor_(HiliteGradient.new(bgColor, Color.white, \v, 64, 0.5))
			.background_(bgColor)
			.lo_(0)
			.hi_(1)
			.action_({ |obj| this.setWaveformZoom(obj.lo, obj.hi); });

		recordButton = GUI.button.new(waveformControlView, Rect.new(0, 0, 75, 25))
			.states_([
				["o", Color.red, Color.new255(25,25,25)],
				["o", Color.black, Color.red]
			])
		    .font_(parent.strongFont)
		    .action_({ |obj| this.record(obj.value.toBool); });

		backButton = GUI.button.new(waveformControlView, Rect.new(0, 0, 50, 25))
			.states_([["<<", Color.white, bgColor]])
		    .font_(parent.strongFont)
		    .action_({ |obj| this.back; })
			.mouseUpAction_({ |obj| s.sendMsg('n_set', playerNodeNum, 'trig', 0) });


		playButton = GUI.button.new(waveformControlView, Rect.new(0, 0, 75, 25))
			.states_([
				[">", Color.green, bgColor],
				[">", Color.black, Color.green]
			])
		    .font_(parent.strongFont)
		    .action_({ |obj| 
		    	this.play(obj.value.toBool); 
		    	this.addLooperEvent('play');
		    });

		forwardButton = GUI.button.new(waveformControlView, Rect.new(0, 0, 50, 25))
			.states_([[">>", Color.white, bgColor]])
		    .font_(parent.strongFont)
		    .action_({ |obj| 
		    	this.forward; 
		    	this.addLooperEvent('forward');
		    })
			.mouseUpAction_({ |obj| s.sendMsg('n_set', playerNodeNum, 'trig', 0) });

		    
		pauseButton = GUI.button.new(waveformControlView, Rect.new(0, 0, 75, 25))
			.states_([
				["||", Color.yellow, bgColor],
				["||", Color.black, Color.yellow]
			])
		    .font_(parent.strongFont)
		    .action_({ |obj| 
		    	this.pause(obj.value.toBool);
		    	this.addLooperEvent('pause', obj.value.toBool);
		    });
		    
		stopButton = GUI.button.new(waveformControlView, Rect.new(0, 0, 75, 25))
			.states_([["[]", Color.white(0.8), bgColor]])
		    .font_(parent.strongFont)
		    .action_({ |obj| 
		    	this.stop; 
				this.addLooperEvent('stop');
		    });

		// CONTROL VIEW
		controlView = GUI.compositeView.new(topView, Rect.new(0, 0, 200, 210))
			.background_(Color.black);
		controlView.decorator_(FlowLayout(controlView.bounds));

		GUI.staticText.new(controlView, Rect.new(0, 0, 40, 20))
            .string_("mod")
		    .stringColor_(Color.white);
		modBusMenu = GUI.popUpMenu.new(controlView, Rect.new(0, 0, 145, 20))
			.items_(parent.audioBusRegister.keys.asArray)
			.background_(bgColor)
		    .font_(parent.controlFont)
			.stringColor_(Color.white)
		    .action_({ |obj| this.setModBus(parent.audioBusRegister[obj.item]); });

		modLevelKnob = EZJKnob.new(controlView, Rect.new(0, 0, 37.5, 73), "mod lev")
			.spec_([0, 300].asSpec)
			.value_(0)
		    .font_(parent.controlFont)
			.knobColor_([Color.clear, Color.white, Color.white.alpha_(0.1), Color.white])
			.stringColor_(Color.white)
			.background_(bgColor)
			.knobAction_({ |obj| 
				this.setModLevel(obj.value);
				this.addLooperEvent('modLevel', obj.value);
			});
		modLagKnob = EZJKnob.new(controlView, Rect.new(0, 0, 37.5, 73), "mod lag")
			.background_(bgColor)
			.spec_([0, 10, 2].asSpec)
			.value_(0.1)
		    .font_(parent.controlFont)
			.stringColor_(Color.white)
			.knobColor_([Color.clear, Color.white, Color.white.alpha_(0.1), Color.white])
			.knobAction_({ |obj| 
				this.setModLag(obj.value);
				this.addLooperEvent('modLag', obj.value);
		});
		speedKnob = EZJKnob.new(controlView, Rect.new(0, 0, 37.5, 73), "speed")
			.spec_([-4, 4].asSpec)
			.value_(1)
			.stringColor_(Color.white)
		    .font_(parent.controlFont)
			.background_(bgColor)
			.knobColor_([Color.clear, Color.white, Color.white.alpha_(0.1), Color.white])
			.knobAction_({ |obj| 
				this.setSpeed(obj.value);
				this.addLooperEvent('speed', obj.value);
			});

		GUI.staticText.new(controlView, Rect.new(0, 0, 37.5, 73))
			.font_(Font.new("Helvetica", 34))
			.stringColor_(Color.white)
			.string_((channelID + 1).asString);
		inputSourceMenu = GUI.popUpMenu.new(controlView, Rect.new(0, 0, 145, 20))
 		    .items_(parent.audioBusRegister.keys.asArray)
			.background_(bgColor)
		    .font_(parent.controlFont)
			.stringColor_(Color.yellow)
		    .action_({ |obj| this.setInputSource(parent.audioBusRegister[obj.item]); });
		GUI.staticText.new(controlView, Rect.new(0, 0, 40, 20))
            .string_("rec")
            .stringColor_(Color.yellow);		    
		inputLevelKnob = EZJKnob.new(controlView, Rect.new(0, 0, 37.5, 73), "in lev")
			.spec_([0, 1].asSpec)
			.value_(1)
			.stringColor_(Color.yellow)
		    .font_(parent.controlFont)
			.background_(bgColor)
			.knobColor_([Color.clear, Color.yellow, Color.yellow.alpha_(0.1), Color.yellow])
			.knobAction_({ |obj| this.setInputLevel(obj.value); });
		preLevelKnob = EZJKnob.new(controlView, Rect.new(0, 0, 37.5, 73), "pre lev")
			.spec_([0, 1].asSpec)
			.value_(0)
			.stringColor_(Color.yellow)
		    .font_(parent.controlFont)
			.background_(bgColor)
			.knobColor_([Color.clear, Color.yellow, Color.yellow.alpha_(0.1), Color.yellow])
			.knobAction_({ |obj| this.setPreLevel(obj.value); });
		GUI.staticText.new(controlView, Rect.new(0, 0, 10, 73))
            .string_("");
		gainKnob = EZJKnob.new(controlView, Rect.new(0, 0, 37.5, 73), "gain")
			.spec_([0, 4].asSpec)
			.value_(1)
			.stringColor_(Color.white)
		    .font_(parent.controlFont)
			.background_(bgColor)
			.knobColor_([Color.clear, Color.white, Color.white.alpha_(0.1), Color.white])
			.knobAction_({ |obj| 
				this.setGain(obj.value);
				this.addLooperEvent('gain', obj.value);
			});
		panKnob = EZJKnob.new(controlView, Rect.new(0, 0, 37.5, 73), "pan")
			.spec_([-1, 1].asSpec)
			.value_(0)
			.stringColor_(Color.white)
		    .font_(parent.controlFont)
			.background_(bgColor)
			.knobColor_([Color.clear, Color.white, Color.white.alpha_(0.1), Color.white])
			.knobAction_({ |obj| 
				this.setPan(obj.value);
				this.addLooperEvent('pan', obj.value);
			});
				
		this.collectControls;
		
	}
	
	collectControls {
		//	I can't tell if this is elegant or horrible
		// collecting all the gui objects in a  pair of Dictionaries
		// fo use with presets.
		playerMap = Dictionary[
			'bufnum'       -> bufferSelectMenu,
			'speed'        -> speedKnob, 
			'start'        -> playerParams['start'], 
			'end'          -> playerParams['end'], 
//			'outBus'       -> 0, // don't mess with the routing!!
//			'trig'         -> 0, // is this even used?
			'modBus'       -> modBusMenu, 
			'modLag'       -> modLagKnob, 
			'modLev'       -> modLevelKnob,
			'inBus'        -> inputSourceMenu,
			'gain'		   -> gainKnob,
			'pan'		   -> panKnob
		];
		recorderMap = Dictionary[
			'bufnum'       -> bufferSelectMenu, 
			'inBus'        -> inputSourceMenu,
			'inLev'        -> inputLevelKnob,
			'stereo'	   -> recorderParams['stereo'],
			'preLev'       -> preLevelKnob
		];
	}
	
	// preset stuff should be handled by the superclass
	
	savePreset { |name|
		var presetName, filePath, fileHandle, params, pipe;
		presetName = name ? "";
		
		if(presetName.size == 0){
			presetName = Date.localtime.stamp;
		};
		
		filePath = saveRoot ++ Platform.pathSeparator ++ presetName;
		
		fileHandle = File.new(filePath, "w");
		
		params = this.getParams;
		
		if(fileHandle.isOpen){
			fileHandle.write(params.asInfString);
			fileHandle.close;
		}{  // the save folder does not exist
			postln("creating save directory " ++ saveRoot);
			pipe = Pipe.new("mkdir -p \"" ++ saveRoot ++ "\"", "w");
			pipe.close;
			fileHandle = File.new(filePath, "w");
			if(fileHandle.isOpen){
				fileHandle.write(params.asInfString);
				fileHandle.close;
			}{
				postln("preset save operation failed");
			};
		};
		
		
	}
	
	loadPreset { |presetName|
			
	}
	
	getParams {
		^Dictionary['\'testParam1\'' -> 1, '\'testParam2\'' -> 55];
	}
	
}

