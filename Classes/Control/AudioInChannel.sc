AudioInputChannel {
	classvar classGroup=102, id=0;
	var <numChannels=8, instGroup=104,
		win, inputMenu, windowName="Input Channel", server, nodeID, groupID, 
		inputChannel=0, outBus=20, instanceNum, instanceName;
	*new {
		id = id + 1;
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
	var instanceNum;
	*new {
		^super.new.init_monoinchannel(id);
	}
	init_monoinchannel { |id|
		instanceNum = id;
		instanceName = "MonoInput" ++ id;
		windowName = "Mono Input Channel";
		this.makeGUI;
		numChannels.do{ |ind|
			inputMenu.items = inputMenu.items.add("in " ++ ind);
		};
		this.startSynth;
		this.addMixerChannel;
	}
	addMixerChannel {
		~mixer.addMonoChannel(instanceName, ~mixer.mixGroup);
		outBus = ~mixer.channels[instanceName].inBus;
	}
	startSynth {
		server.sendMsg('s_new', 's_monoInputChannel', nodeID, 0, instGroup,
			'outBus', outBus, 'channel', inputChannel);
	}
}

StereoInputChannel : AudioInputChannel {
	*new {
		^super.new.init_stereoinchannel(id);
	}
	init_stereoinchannel { |id|
		instanceNum = id;
		instanceName = "StereoInput" ++ id;
		windowName = "Stereo Input Channel";
		this.makeGUI;
		numChannels.do{ |ind|
			if(ind < (server.options.numInputBusChannels - 1)){
				inputMenu.items = inputMenu.items.add("left " ++ ind ++ " right " ++ (ind + 1));
			};
		};
		this.startSynth;
		this.addMixerChannel;
	}
	addMixerChannel {
		~mixer.addStereoChannel(instanceName, ~mixer.mixGroup);
		outBus = ~mixer.channels[instanceName].inBus;
	}
	startSynth {
		server.sendMsg('s_new', 's_stereoInputChannel', nodeID, 0, instGroup,
			'outBus', outBus, 'channel', inputChannel);
	}

}
   