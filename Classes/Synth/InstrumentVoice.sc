InstrumentVoice {
	/*
		i will contain all functionality common to the following "voice" classes:
			DSBase            .... the common base for all drum synth voices     
			SampleLooper      .... the channels and not the Sampler container classs
			WavetableSynth    .... is inherited by WavetableSynthFilter
			GravityGridPlayer .... for semantic consistency

	*/
	var noteOnFunction, noteOffFunction, ccFunction, bendFunction, afterTouchFunction;
	var sep, saveRoot, rawParams, formattedParams, nodeNum, startParams, s;
	*new {
		^super.new.init_instrumentbase;
	}
	
	init_instrumentbase {
		noteOnFunction = { |src,chan,num,vel|
			[src,chan,num,vel].postln;
		};
		noteOffFunction = { |src,chan,num,vel|
			[src,chan,num,vel].postln;
		};
		ccFunction = { |src,chan,num,val|
			[src,chan,num,val].postln;
		};
		bendFunction = { |src,chan,val|
			[src,chan,val].postln;
		};
		afterTouchFunction = { |src,chan,val|
			[src,chan,val].postln;
		}; 
		sep = Platform.pathSeparator;
		saveRoot = Platform.userAppSupportDir ++ sep ++ "Presets";
		s = Server.default;
		postln(this.class.asString ++ " initialized");
	}
	
	setParam { |param,val|
		startParams[param] = val;
		s.sendMsg('n_set', nodeNum, param, val);
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
		noteOnFunction.value(src,chan,num,vel);
	}
	
	noteOff { |src,chan,num,vel|
		noteOffFunction.value(src,chan,num,vel);
	}
	
	bend { |src,chan,val|
		bendFunction.value(src,chan,val);
	}
	
	cc { |src,chan,num,val|
		ccFunction.value(src,chan,num,val);
	}
	
	afterTouch { |src,chan,val|
		afterTouchFunction.value(src,chan,val);
	}
	
}