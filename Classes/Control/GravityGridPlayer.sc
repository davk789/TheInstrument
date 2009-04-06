GravityGridPlayer {
	var <bufnum=75, maxSize=16, size=5, server, massCoordView, massWeightView;
	*new {
		^super.new.init_gravitygridplayer;
	}
	init_gravitygridplayer {
		server = Server.default;
		this.makeGUI;
		this.initBuffer;
	}
	initBuffer {
		server.sendMsg('b_alloc', bufnum, 1 + (maxSize * 3));
		this.updateBuffer;
	}
	updateBuffer {
		server.listSendMsg(['b_setn', bufnum, 1 + (size * 3), size] ++ this.getParamList);
	}
	getParamList {
		^lace([massCoordView.value[0], massCoordView.value[1], massWeightView.value]);
	}
	start {}
	stop {}
	setNewX {}
	setNewY {}
	addMass { }
	makeGUI {
		var win;
		win = GUI.window.new("Gravity Grid", Rect.new(200.rand, 200.rand, 325, 200))
			.front;
		win.view.decorator_(FlowLayout(win.view.bounds))
			.background_(Color.black);
		massCoordView = GUI.envelopeView.new(win, Rect.new(0, 0, 150, 150))
			.value_([[1.frand, 1.frand, 1.frand, 1.frand, 1.frand], [1.frand, 1.frand, 1.frand, 1.frand, 1.frand]])
			.thumbSize_(6)
			.fillColor_(Color.green.alpha_(0.7))
			.strokeColor_(Color.green)
			.background_(Color.black.alpha_(0.9))
			.drawLines_(false)
			.mouseUpAction_({ |obj| this.updateBuffer; });
		massWeightView = GUI.multiSliderView.new(win, Rect.new(0, 0, 150, 150))
			.fillColor_(Color.green.alpha_(0.7))
			.strokeColor_(Color.green)
			.background_(Color.black.alpha_(0.9))
			.valueThumbSize_(3.4)
			.value_(Array.fill(massCoordView.size, { |ind| 1.frand; }))
			.isFilled_(true)
			.showIndex_(true)
			.mouseUpAction_({ |obj| this.updateBuffer; });
		massWeightView.indexThumbSize_(150 / massWeightView.value.size);


	}
}
