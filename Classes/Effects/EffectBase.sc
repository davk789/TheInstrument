EffectBase {
	classvar parent;
	var server, saveRoot, sep, inputName, inputNumber, groupID, <nodeID, 
		startParams, paramControls, synthdefName, <win, 
		presetRow, saveButton, presetNameField, presetMenu,
		<winBounds, addGUIControls; // to be set by subclasses
	
	*new { |par, group, name, ind|
		parent = par;
		^super.new.init_effectbase(group, name, ind);
	}
	
	init_effectbase { |group, name, ind|
		sep = Platform.pathSeparator;
		saveRoot = Platform.userAppSupportDir ++ sep ++ "Presets" ++ sep ++ this.class.asString;
		server = Server.default;
		nodeID = server.nextNodeID;
		winBounds = Rect.new(500.rand, 500.rand, 400, 120);
		groupID = group;
		inputName = name;
		inputNumber = ind;
	}
	
	startSynth {
		var numChan;
		numChan = parent.mixer.channels[inputName].numChannels;
		if(numChan == 1){
			server.listSendMsg(['s_new', synthdefName, nodeID, 0, groupID] ++ startParams.getPairs);
		}{
			server.sendMsg('g_new', nodeID, 0, groupID);
			numChan.do{ |ind|
				startParams['bus'] = startParams['bus'] + ind;
				server.listSendMsg(
					['s_new', synthdefName, server.nextNodeID, 0, nodeID] ++ startParams.getPairs;
				);
			};
		};
	}
	
	releaseSynth {
		if(parent.numChannels == 1){
			server.sendMsg('n_free', nodeID);	
		}{
			server.sendMsg('g_free', nodeID);	
		};
		win.close;
	}
	
	initGUI {

		win = GUI.window.new(inputName ++ " channel, slot " ++ inputNumber, winBounds).front;
		win.view
			.background_(Color.black)
			.decorator_(FlowLayout(win.view.bounds));
		presetRow = GUI.hLayoutView.new(win, Rect.new(0, 0, win.view.bounds.width, 20))
			.background_(Color.white.alpha_(0.8));
		saveButton = GUI.button.new(presetRow, Rect.new(0, 0, 75, 0))
			.states_([["save", Color.black, Color.green]])
			.action_({ |obj| this.savePreset(presetNameField.string); });
		presetNameField = GUI.textField.new(presetRow, Rect.new(0, 0, 75, 0))
			.action_({ |obj| this.savePreset(obj.string); })
			.string_("<>");
		presetMenu = GUI.popUpMenu.new(presetRow, Rect.new(0, 0, 230, 0))
			.items_(this.getPresetList)
			.action_({ |obj| this.loadPreset(obj.item) });
	}

	getPresetList {
		^(saveRoot ++ sep ++ "*").pathMatch.collect{ |obj,ind| obj.split($/).last; };
	}
	
	getParams {
		var ret;
		ret = Dictionary.new;
		// iterate over the controls, not the parameters
		paramControls.keysValuesDo{ |key,obj,ind| 
			ret = ret.add('\'' ++ key ++ '\'' -> obj.value);
		};
		^ret;
	}
	
	savePreset { |name| 
		// copied from DrumSynth... preset support should exist as a static class or a global object
		var params, fileName, filePath, fh, pipe;

		params = this.getParams;
		if(name == "<>"){
			fileName = Date.localtime.stamp;
		}{
			fileName = name;
		};
		
		filePath = saveRoot ++ sep ++ fileName;
		fh = File.new(filePath, "w");
		if(fh.isOpen){
			fh.write(params.asInfString);
			fh.close;
		}{
			postln("creating save directory " ++ saveRoot);
			pipe = Pipe.new("mkdir -p \"" ++ saveRoot ++ "\"", "w");
			pipe.close;
			fh = File.new(filePath, "w");
			if(fh.isOpen){
				fh.write(params.asInfString);
				fh.close
			}{
				postln("preset save operation failed");
			};
			
		};
		presetMenu.items_(this.getPresetList);
		presetNameField.string_("<>");
	}

	loadPreset { |presetName|
		var preset;
		preset = (saveRoot ++ sep ++ presetName).load;
		preset.asInfString.postln;
		preset.keysValuesDo{ |key,val,ind|
			paramControls['key'].valueAction_(val);
		}
		//startParams = preset;
		//this.refreshParams;
		
	}

	/*refreshParams {
		startParams.keysValuesDo{ |key,val,ind|
			if(key.notNil){
				paramControls.value = val;
				server.sendMsg('n_set', nodeID, key, val);
			};
		}
	}*/
	
	makeGUI {
		if(win.isNil){
			this.initGUI;
			addGUIControls.value;
		};
		if(win.isClosed){
			this.initGUI;
			addGUIControls.value;
		}{
			win.front;
		};
	}

}