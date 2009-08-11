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
		server.listSendMsg(['s_new', synthdefName, nodeID, 0, groupID] ++ startParams.getPairs);
	}
	
	releaseSynth {
		server.sendMsg('n_free', nodeID);
	}
	
	initGUI {
		win = GUI.window.new(inputName ++ " channel, slot " ++ inputNumber, winBounds).front;
		win.view
			.background_(Color.black)
			.decorator_(FlowLayout(win.view.bounds));
	}
}