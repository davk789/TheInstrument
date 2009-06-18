AudioInputChannel {
	classvar classGroup=102, id=0;
	var parent, <numChannels=8, instGroup=104, synthDefName, addCommand,
		win, inputMenu, windowName="Input Channel", server, nodeID, groupID, 
		inputChannel=0, outBus=20, instanceNum, instanceName;
	*new { |par|
		id = id + 1;
		^super.new.init_audioinchannel(par);
	}
	init_audioinchannel { |par|
		parent = par;
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
	startSynth {
		server.sendMsg('s_new', synthDefName, nodeID, 0, instGroup,
			'outBus', outBus, 'channel', inputChannel);
	}
	addMixerChannel {
		addCommand.value;
		outBus = parent.mixer.channels[instanceName].inBus;
	}

}

MonoInputChannel : AudioInputChannel {
	var instanceNum;
	*new { |par|
		^super.new(par).init_monoinchannel(id);
	}
	init_monoinchannel { |id|
		instanceNum = id;
		instanceName = "MonoInput" ++ id;
		windowName = "Mono Input Channel";
		synthDefName = 's_monoInputChannel';
		addCommand = { 
			parent.mixer.addMonoChannel(instanceName, instGroup, true) 
		};
		this.makeGUI;
		numChannels.do{ |ind|
			inputMenu.items = inputMenu.items.add("in " ++ ind);
		};
		this.addMixerChannel;
		this.startSynth;
	}
}

StereoInputChannel : AudioInputChannel {
	*new { |par|
		^super.new(par).init_stereoinchannel(id);
	}
	init_stereoinchannel { |id|
		instanceNum = id;
		instanceName = "StereoInput" ++ id;
		windowName = "Stereo Input Channel";
		synthDefName = 's_stereoInputChannel';
		addCommand = {
			parent.mixer.addStereoChannel(instanceName, instGroup, true);
		};
		this.makeGUI;
		numChannels.do{ |ind|
			if(ind < (server.options.numInputBusChannels - 1)){
				inputMenu.items = inputMenu.items.add("left " ++ ind ++ " right " ++ (ind + 1));
			};
		};
		this.addMixerChannel;
		this.startSynth;
	}
}
   
