EffectBase {
	var server, inputName, inputNumber, groupID, <nodeID, startParams, synthdefName,
		win;
	*new { |group, name, ind|
		^super.new.init_effectbase(group, name, ind);
	}
	init_effectbase { |group, name, ind|
		server = Server.default;
		nodeID = server.nextNodeID;
		groupID = group;
		inputName = name;
		inputNumber = ind;
	}
	startSynth {
		server.listSendMsg(['s_new', synthdefName, nodeID, 0, groupID] ++ startParams);
	}
	releaseSynth {
		server.sendMsg('n_free', nodeID);
	}
	initGUI {
		win = GUI.window.new(inputName ++ " channel, slot " ++ inputNumber, Rect.new(500.rand, 500.rand, 400, 120)).front;
		win.view
			.background_(Color.black)
			.decorator_(FlowLayout(win.view.bounds));
	}
}