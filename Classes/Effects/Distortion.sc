Distortion {
	var server, groupID, inputName, inputNumber, groupID, <nodeID, bus,
		win;
	*new { |group,name,ind|
		^super.new.init_distortion(group, name, ind);
	}
	init_distortion { |group,name,ind|
		server = Server.default;
		nodeID = server.nextNodeID;
		groupID = group;
		inputName = name;
		inputNumber = ind;
	//	bus = ~mixer.channels[inputName].inBus;
		"init distortion".postln;
		this.initGUI;
	}
	initGUI {
		win = GUI.window.new("Distortion", Rect.new(100, 500, 500, 300))
			.front;
		win.view.decorator = FlowLayout(win.view.bounds);
	}

}
