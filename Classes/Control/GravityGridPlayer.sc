GravityGridPlayer {
	var <bufnum=75, parent, nodeNum, groupNum, maxSize=16, server, massCoords,
		outBus=0, resetInBus=22, rate=1, newX=0.5, newY=0.5, bufnum=75, resetRate=1,
		massCoordView, massWeightView, startButton, resetInBusMenu, rateKnob, resetRateKnob, newXKnob, newYKnob;
	*new { |par|
		^super.new.init_gravitygridplayer(par);
	}
	init_gravitygridplayer { |par|
		server = Server.default;
		parent = par;
		nodeNum = server.nextNodeID;
		groupNum = server.nextNodeID;
		massCoords = Array.fill(5, { |ind| (150.rand @ 150.rand); });
		server.sendMsg('g_new', groupNum, 0, 1);
		this.addMixerChannel;
		this.makeGUI;
		this.initBuffer;
	}
	addMixerChannel {
		parent.mixer.addMonoChannel("GravityGrid", groupNum);
		outBus = parent.mixer.channels["GravityGrid"].inBus;
	}
	initBuffer {
		server.sendMsg('b_alloc', bufnum, 1 + (maxSize * 3));
		this.updateBuffer;
	}
	updateBuffer {
		server.listSendMsg(['b_setn', bufnum, 0, 1 + (massCoords.size * 3), massCoords.size] ++ this.getParamList);
	}
	start {
		server.sendMsg('s_new', 's_gravityGrid', nodeNum, 0, groupNum, 
			'outBus', outBus, 'resetInBus', resetInBus, 'rate', rate, 
			'newX', newX, 'newY',  newY, 'bufnum', bufnum, 'resetRate', resetRate);
	}
	stop {
		server.sendMsg('n_free', nodeNum);
	}
	setPlay { |val|
		if(val == 1){
			this.start;
		}{
			this.stop;
		};
	}
	setResetInBus { |val|
		resetInBus = val;
		server.sendMsg('n_set', nodeNum, 'resetInBus', resetInBus); 
	}
	setRate { |val| 
		rate = val;
		server.sendMsg('n_set', nodeNum, 'rate', rate); 
	}
	setNewX { |val| 
		newX = val;
		server.sendMsg('n_set', nodeNum, 'newX', newX); 
	}
	setNewY { |val| 
		newY = val;
		server.sendMsg('n_set', nodeNum, 'newY', newY); 
	}
	setResetRate { |val|
		resetRate = val;
		server.sendMsg('n_set', nodeNum, 'resetRate', resetRate);
	}
	getParamList {
		var x, y, wgt;
		x = this.displayToCoords(massCoordView.coords.collect({ |obj,ind| obj.x; }), massCoordView.bounds.width);
		y = this.displayToCoords(massCoordView.coords.collect({ |obj,ind| obj.y; }), massCoordView.bounds.height);
		wgt = massWeightView.value;
		^lace([x, y, wgt]);
	}
	displayToCoords { |val,range=150|
		^val / ((range / 2) - 1);
	}
	updateMassCoordView {
		massCoordView.currentIndex = massWeightView.index;
	}
	updateMassWeightView {
		var wgt;
		massCoords = massCoordView.coords;
		while{massCoords.size < massWeightView.value.size}{
			massWeightView.value.pop;
		};
		while{massCoords.size > massWeightView.value.size}{
			massWeightView.value = massWeightView.value.add(1.frand);
		};
		massWeightView.indexThumbSize_(150 / (massWeightView.value.size * 1.1));
		//massWeightView.index = massCoordView.currentIndex;
	}
	makeGUI {
		var win;
		win = GUI.window.new("Gravity Grid", Rect.new(200.rand, 200.rand, 325, 275))
			.front;
		win.view.decorator_(FlowLayout(win.view.bounds))
			.background_(Color.black);
/**
The massCoordView scrambles its point array when editing the control, so it can't update the massWeightView on editing. This should be fixed later.
 */
		massCoordView = MarkerArea.new(win, Rect.new(0, 0, 150, 150))
			.mouseMoveAction_({ |obj,x,y,mod| this.updateMassWeightView; })
			.mouseUpAction_({ |obj| this.updateBuffer; });
		massCoordView.coords_(massCoords);
		massWeightView = GUI.multiSliderView.new(win, Rect.new(0, 0, 150, 150))
			.fillColor_(Color.yellow.alpha_(0.7))
			.strokeColor_(Color.yellow)
			.background_(Color.black.alpha_(0.9))
			.valueThumbSize_(3.4)
			.value_(Array.fill(massCoordView.coords.size, { |ind| 1.frand; }))
			.isFilled_(true)
			.showIndex_(true)
			.mouseDownAction_({ |obj| this.updateMassCoordView})
			.mouseUpAction_({ |obj| this.updateBuffer; });
		massWeightView.indexThumbSize_(150 / massWeightView.value.size);
		startButton = GUI.button.new(win, Rect.new(0, 0, 125, 20))
			.states_([
				["play", Color.yellow, Color.new255(30, 30, 0, 180)],
				["stop", Color.black, Color.yellow]])
			.action_({ |obj| this.setPlay(obj.value); });
		resetInBusMenu = GUI.popUpMenu.new(win, Rect.new(0, 0, 165, 20))
			.items_(parent.audioBusRegister.keys.asArray)
			.action_({ |obj| this.setResetInBus(parent.audioBusRegister[obj.item]) });
		rateKnob = EZJKnob.new(win, Rect.new(0, 0, 37.5, 73), "rate")
			.spec_([0.01, 1, 'exponential'].asSpec)
			.value_(1)
			.knobColor_([Color.white.alpha_(0.3), Color.yellow, Color.black, Color.yellow])
			.stringColor_(Color.yellow)
			.knobAction_({ |obj| this.setRate(obj.value); });
		newXKnob = EZJKnob.new(win, Rect.new(0, 0, 37.5, 73), "newX")
			.spec_('pan'.asSpec)
			.value_(2.frand - 1)
			.stringColor_(Color.yellow)
			.knobColor_([Color.white.alpha_(0.3), Color.yellow, Color.black, Color.yellow])
			.knobAction_({ |obj| this.setNewX(obj.value); });
		newYKnob = EZJKnob.new(win, Rect.new(0, 0, 37.5, 73), "newY")
			.spec_('pan'.asSpec)
			.value_(2.frand - 1)
			.stringColor_(Color.yellow)
			.knobColor_([Color.white.alpha_(0.3), Color.yellow, Color.black, Color.yellow])
			.knobAction_({ |obj| this.setNewY(obj.value); });
		resetRateKnob = EZJKnob.new(win, Rect.new(0, 0, 37.5, 73), "reset")
			.spec_([0.01,30].asSpec)
			.value_(1)
			.stringColor_(Color.yellow)
			.knobColor_([Color.white.alpha_(0.3), Color.yellow, Color.black, Color.yellow])
			.knobAction_({ |obj| this.setResetRate(obj.value); });

	}
}
