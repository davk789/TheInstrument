Compressor {
	var server, inputName, inputNumber, groupID, <nodeID, 
		bus=20, controlBus=20, mix=1, 
		threshold=0.6, slopeBelow=1, slopeAbove=0.2, 
		clampTime=0.005, relaxTime=0.01,
		win;
	*new { |group, name, ind|
		^super.new.init_compressor(group, name, ind);
	}
	init_compressor { |group, name, ind|
		server = Server.default;
		nodeID = server.nextNodeID;
		groupID = group;
		inputName = name;
		inputNumber = ind;
		
		this.initGUI;
	}
	initGUI {
		win = GUI.window.new("Compressor", Rect.new(500.rand, 500.rand, 400, 300)).front;
		win.view
			.background_(Color.black)
			.decorator_(FlowLayout(win.view.bounds));
	}
	makeGUI {
		var fullWidth, controlRow;
		if(win.isClosed){ 
			win = nil;
			this.initGUI;
		};
		fullWidth = win.view.bounds.width - 10;
		GUI.staticText.new(win, Rect.new(0, 0, fullWidth-100, 20))
			.stringColor_(Color.yellow)
			.string_(inputName ++ " channel, slot " ++ inputNumber)
			.align_('center');
		GUI.button.new(win, Rect.new(0, 0, 90, 20))
			.states_([
				["bypass", Color.black, Color.white(0.6).alpha_(0.6)],
				["bypass", Color.black, Color.yellow]
			]);
		controlRow = GUI.hLayoutView.new(win, Rect.new(0, 0, fullWidth, 80))
		    .background_(Color.blue(0.2, alpha:0.3));
		EZJKnob.new(controlRow, Rect.new(0, 0, 40, 80), "thresh");
		EZJKnob.new(controlRow, Rect.new(0, 0, 40, 80), "sl-bel");
		EZJKnob.new(controlRow, Rect.new(0, 0, 40, 80), "sl-ab");
		EZJKnob.new(controlRow, Rect.new(0, 0, 40, 80), "clampT");
		EZJKnob.new(controlRow, Rect.new(0, 0, 40, 80), "relaxT");
	}
}