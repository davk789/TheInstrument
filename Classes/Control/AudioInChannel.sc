AudioInputChannel {
	classvar classGroup=102;
	var <numChannels=8, instGroup=104,
		win, inputMenu, windowName="Input Channel", server, nodeID, groupID, inputChannel=0, outBus=20;
	*new {
		^super.new.init_audioinchannel;
	}
	init_audioinchannel {
		server = Server.default;
		numChannels = server.options.numInputBusChannels;
		nodeID = server.nextNodeID;
		server.sendMsg('g_new', classGroup, 0, 1);
		server.sendMsg('g_new', instGroup, 0, classGroup);
		
	}
	setInputChannel { |val|
		inputChannel = val;
		server.sendMsg('n_set', nodeID, 'inputChannel', inputChannel);
	}
	makeGUI {
		win = GUI.window.new(windowName, Rect.new(200, 600, 200, 100)).front;
		win.view
			.decorator_(FlowLayout(win.view.bounds))
			.background_(Color.black);
		inputMenu = GUI.popUpMenu.new(win, Rect.new(0, 0, 200, 20))
			.action_({ |obj| this.setInputChannel(obj.value); });
	}
	releaseSynth {
		server.sendMsg('n_free', nodeID);
	}
}

MonoInputChannel : AudioInputChannel {
	*new {
		^super.new.init_monoinchannel;
	}
	init_monoinchannel {
		windowName = "Mono Input Channel";
		this.makeGUI;
		numChannels.do{ |ind|
			inputMenu.items = inputMenu.items.add("in " ++ ind);
		};
		this.startSynth;
	}
	addMixerChannel {
		~mixer.addMonoChannel("MonoInputChannel", ~mixer.mixGroup);
		outBus = ~mixer.channels["MonoInputChannel"].inBus;
	}
	startSynth {
		server.sendMsg('s_new', 's_monoInputChannel', nodeID, 0, instGroup,
			'outBus', outBus, 'channel', inputChannel);
	}
}

StereoInputChannel : AudioInputChannel {
	*new {
		^super.new.init_stereoinchannel;
	}
	init_stereoinchannel {
		windowName = "Stereo Input Channel";
		this.makeGUI;
		numChannels.do{ |ind|
			if(ind < (server.options.numInputBusChannels - 1)){
				inputMenu.items = inputMenu.items.add("left " ++ ind ++ " right " ++ (ind + 1));
			};
		};
		this.startSynth;
	}
	addMixerChannel {
		~mixer.addStereoChannel("StereoInputChannel", ~mixer.mixGroup);
		outBus = ~mixer.channels["StereoInputChannel"].inBus;
	}
	startSynth {
		server.sendMsg('s_new', 's_stereoInputChannel', nodeID, 0, instGroup,
			'outBus', outBus, 'channel', inputChannel);
	}

}
