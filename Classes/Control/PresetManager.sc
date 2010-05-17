PresetManager { // this should eliminate redundant code relating to saving/recalling presets
	// actually, i think getting all the instrument classes to inherit from a common base
	// would be a better way to approach this problem
	var saveRoot, parent, saveRoot, rawParams, fParams;
	var <>background, <>stringColor;
	// GUI
	var saveButton, saveField, loadMenu, parent;
	*new { |par|
		^super.new.init_presetmanager(par);
	}
	
	init_presetmanager { |par|
		var sep;
		sep = Platform.pathSeparator;
		parent = par;
		background = Color.blue.alpha_(0.2);
		stringColor = Color.white;
		saveRoot = Platform.userAppSupportDir ++ sep ++ "Presets" ++ sep ++ parent.presetFolder;
	}
	
	savePreset { |name|
		var presetName, filePath, fileHandle, params, pipe;
		presetName = name ? "";
		
		if(presetName.size == 0){
			presetName = Date.localtime.stamp;
		};
		
		filePath = saveRoot ++ Platform.pathSeparator ++ presetName;
		
		fileHandle = File.new(filePath, "w");
		
		params = fParams;
		
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
		params.keysValuesDo{ |key,val,ind|
			ret = ret.add('\'' ++ key ++ '\'' -> val);
		}
		^ret;
	}
	
	params_ { |params|
		// the preset manasger needs to ba able to ask the pardent for its params
		rawParams = params;
		fParams = this.formatParams(rawParams);
	}
	
	params {
		^rawParams;
	}
	
	makeGUI { |parent|
		GUI.staticText.new(parent, Rect.new(0, 0, 25, 0))
			.string_("");
		saveButton = GUI.button.new(parent, Rect.new(0, 0, 45, 0))
		    .font_(parent.controlFont)
			.states_([["save", Color.white, Color.blue.alpha_(0.2)]])
		    .action_({ |obj| 
		    	this.savePreset(saveField.string);
		    	saveField.string = "";
		    });

		saveField = GUI.textField.new(parent, Rect.new(0, 0, 90, 0))
			.action_({ |obj| 
				this.savePreset(obj.string);
				obj.string = "";
			});

		loadMenu = GUI.popUpMenu.new(parent, Rect.new(0, 0, 145, 0))
			.items_(["this will have the presets listed", "some day"])
			.background_(background)
		    .font_(parent.controlFont)
			.stringColor_(stringColor)
		    .action_({ |obj| this.loadPreset(obj.item); });

		
	}

}