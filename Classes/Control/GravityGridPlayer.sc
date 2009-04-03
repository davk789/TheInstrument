GravityGridPlayer {
	var <bufnum=75, maxSize=16, size=5, server, massCoordView;
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
		server.listSendMsg(['b_setn', bufnum, 1 + (size * 3), size] ++ massCoordView.value.lace);
	}
	start {}
	stop {}
	setNewX {}
	setNewY {}
	setGridBuffer { |val|
		postln("calling setGridBuffer with data index: " ++ val.index);
	}
	addMass { }
	makeGUI {
		var win;
		win = GUI.window.new("Gravity Grid", Rect.new(200.rand, 200.rand, 300, 200))
			.front;
		win.view.decorator_(FlowLayout(win.view.bounds))
			.background_(Color.black);
		massCoordView = GUI.envelopeView.new(win, Rect.new(0, 0, 150, 150))
			.value_([[1.frand, 1.frand, 1.frand, 1.frand, 1.frand], [1.frand, 1.frand, 1.frand, 1.frand, 1.frand]])
			.thumbSize_(6)
			.fillColor_(Color.green)
			.strokeColor_(Color.green)
			.background_(Color.black.alpha_(0.9))
			.drawLines_(false)
			.action_({ |obj| this.setGridBuffer(obj); })
			.mouseDownAction_({ |obj| obj.index.postln; });
	}
}
