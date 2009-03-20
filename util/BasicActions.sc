GenericActionButton {
	var <>mouseDownAction, <>mouseUpAction;
	*new {
		^super.new.init_genericactionbutton;
	}
	init_genericactionbutton {
		mouseDownAction = { |val| val.postln; };
		mouseUpAction = { |val| val.postln; };
		this.makeGUI;
	}
	makeGUI {
		var win, but;
		win = GUI.window.new("act", Rect.new(500.rand, 500.rand, 300, 200)).front;
		but = GUI.button.new(win, Rect.new(0, 0, 290, 190))
			.states_([["test", Color.green, Color.black]])
			.mouseDownAction_({ |obj| mouseDownAction.value(obj.value); })
			.mouseUpAction_({ |obj| mouseUpAction.value(obj.value); });
	}
}