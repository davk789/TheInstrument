EffectBase {
	var parent, server, inputName, inputNumber, groupID, <nodeID, startParams, synthdefName,
		<win, <winBounds;
	*new { |par, group, name, ind|
		^super.new.init_effectbase(par, group, name, ind);
	}
	init_effectbase { |par, group, name, ind|
		parent = par;
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
	}
	
	initGUI {
		win = GUI.window.new(inputName ++ " channel, slot " ++ inputNumber, winBounds).front;
		win.view
			.background_(Color.black)
			.decorator_(FlowLayout(win.view.bounds));
	}
}