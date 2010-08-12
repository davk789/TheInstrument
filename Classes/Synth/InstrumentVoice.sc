InstrumentVoice {
	/*
		i will contain all functionality common to the following "voice" classes:
			DSBase            .... the common base for all drum synth voices     
			SampleLooper      .... the channels and not the Sampler container classs
			WavetableSynth    .... is inherited by WavetableSynthFilter
			GravityGridPlayer .... for semantic consistency

	*/
	classvar server, synthDefName;
	var noteOnFunction, noteOffFunction, ccFunction, bendFunction, afterTouchFunction;
	var sep, saveRoot, rawParams, formattedParams, nodeNum, groupID, startParams;
	var <>win;
	var activeNotes, lastNote, parent, outBus=22;
	*new { |par|
		server = Server.default;
		^super.new.init_instrumentbase(par);
	}
	
	init_instrumentbase { |par|
		parent = par;
		activeNotes = Dictionary.new;
		startParams = Dictionary.new;
		sep = Platform.pathSeparator;
		saveRoot = Platform.userAppSupportDir ++ sep ++ "Presets";
		groupID = server.nextNodeID;

		server.sendMsg('g_new', groupID, 0, 1);
		
		// the subclass needs to call this.addMixerChannel
		postln(this.class.asString ++ " initialized");
	}
	
	initializeMIDI {
		noteOnFunction = {};
		noteOffFunction = {};
		ccFunction = {};
		bendFunction = {};
		afterTouchFunction = {};
	}

	addMixerChannel { // must be called by the subclass, after synthDefName is set
		parent.mixer.addMonoChannel(synthDefName, 0);
		outBus = parent.mixer.channels[synthDefName].inBus;
	}
	
	setParam { |param,val|
		startParams[param] = val;
		server.sendMsg('n_set', groupID, param, val);
	}
	
	savePreset { |name|
		var presetName, filePath, fileHandle, params, pipe;
		presetName = name ? "";
		
		if(presetName.size == 0){
			presetName = Date.localtime.stamp;
		};
		
		filePath = saveRoot ++ Platform.pathSeparator ++ presetName;
		
		fileHandle = File.new(filePath, "w");
		
		params = formattedParams;
		
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
	
	formatParams { |params|
		var ret;
		ret = Dictionary.new;
		rawParams.keysValuesDo{ |key,val,ind|
			ret = ret.add('\'' ++ key ++ '\'' -> val);
		}
		^ret;
	}
	
	loadPreset { |presetName|
		var preset;
		preset = (saveRoot ++ sep ++ presetName).load;
		rawParams = preset;
		this.refreshValues;
	}
	
	getParams {
		// how should i be handled?	
	}

	noteOn { |src,chan,num,vel|
		// pre-process params if needed
		noteOnFunction.value(src,chan,num,vel);
		// voice management
		this.addActiveNote(num, server.nextNodeID);
		// server messaging
		this.doNoteOn(src,chan,num,vel);
		// add looper support here
	}
	
	noteOff { |src,chan,num,vel|
		// pre-process params if needed
		noteOffFunction.value(src,chan,num,vel);
		// voice management
		this.removeActiveNote(num);
		// server messaging
		this.doNoteOff(src,chan,num,vel);
		// add looper support here
	}
	
	bend { |src,chan,val|
		bendFunction.value(src,chan,val);
		this.doBend(src,chan,val);
		// add looper support here
	}
	
	cc { |src,chan,num,val|
		ccFunction.value(src,chan,num,val);
		this.doCC(src,chan,num,val);
		// add looper support here
	}
	
	afterTouch { |src,chan,val|
		afterTouchFunction.value(src,chan,val);
		this.doAfterTouch(src,chan,val);
		// add looper support here
	}

	// midi functions

	doNoteOn { |src,chan,num,vel|
		[src,chan,num,vel].postln;
		server.listSendMsg(['s_new', synthDefName, activeNotes[num].last, 0, groupID] ++ startParams);
		server.sendMsg('n_set', activeNotes[num].last, 'gate', 1);
	}

	doNoteOff { |src,chan,num,vel|
		[src,chan,num,vel].postln;
		server.sendMsg('n_set', activeNotes[num].last, 'gate', 0);
	}

	doCC { |src,chan,num,val|
		[src,chan,num,val].postln;
	}

	doBend { |src,chan,val|
		[src,chan,val].postln;
	}

	doAfterTouch { |src,chan,val|
		[src,chan,val].postln;
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

	removeActiveNote { |noteNum|
		if(lastNote.size == 1){
			activeNotes.removeAt(noteNum);
		}{
			activeNotes[noteNum].removeAt(0);
		};
	}

	//
	
}