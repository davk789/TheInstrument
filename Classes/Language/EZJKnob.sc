
KnobWrapper {
	var containerView, knob, numberBox, label;
	*new { |parent,bounds|
		^super.new.init_knobwrapper(parent,bounds);
	}
	init_knobwrapper { |parent,bounds|
		//knob = GUI.knob.new(parent,bounds);
		containerView = GUI.compositeView.new(parent, bounds);
		//containerView.decorator = FlowLayout(containerView.view.bounds);
		//label = GUI.staticText.new(containerView, Rect.new(0, 0, 0, bounds.height * 0.2))
		//	.string_("test");
		knob = GUI.knob.new(containerView, Rect.new(0, 0, bounds.width, bounds.height * 0.4));
		//numberBox = GUI.numberBox.new(containerView, Rect.new(0, 0, 0, bounds.height * 0.2));
	}
}

EZJKnob {
    var <knobView, <label, <knob, <numberBox, labelText, >knobAction, controlSpec,
		prValue=0;
    *new { |parent, dimensions, lab|
        ^super.new.init_ezknobDK(parent, dimensions, lab);
    }
    init_ezknobDK { |parent, dimensions, lab|
        labelText = lab ? "Knob";
        knobAction = { |val| val.postln; };
        controlSpec = [0,1].asSpec;

        this.addControls(parent, dimensions);
        numberBox.value = knob.value;
    }
    addControls { |parent, dimensions|
        knobView = GUI.compositeView.new(parent, dimensions).background_(Color.blue(0.2, alpha:0.1));
        label = GUI.staticText.new(knobView, Rect.new(0, 0, dimensions.width, knobView.bounds.height / 4.2))
            .string_(labelText)
			.align_('center');
        knob = GUI.knob.new(knobView, Rect.new(0, knobView.bounds.height * 0.28, dimensions.width, knobView.bounds.height / 2.1))
            .action_({|obj|
				if(controlSpec.isKindOf(ControlSpec)){
					prValue = controlSpec.map(obj.value);
				}{
					prValue = obj.value;
				};
				//postln("prValue: " ++ prValue);
              	knobAction.value(prValue);
              	numberBox.value = prValue;
            });
        numberBox = GUI.numberBox.new(knobView, Rect.new(0, knobView.bounds.height * 0.8, dimensions.width, knobView.bounds.height / 4.2))
        	.action_({ |obj|
        		prValue = obj.value;
        		knobAction.value(prValue);
        		knob.value = prValue;
        	});
    }
	knobValueAction {
		knobAction.value(prValue);
	}
	zeroOneValue_ { |val|
		this.value_(controlSpec.map(val));
	}
	
	action_ { |func|
		knobAction = func;
	}
	
	action {
		^knobAction;
	}
	
	value_ { |val|
		prValue = val;
		if(controlSpec.isKindOf(ControlSpec)){
			knob.value = controlSpec.unmap(prValue);
		}{
			knob.value = prValue;
		};
		numberBox.value = prValue;
	}
	
	valueAction_ { |val|
		this.value = val;
		this.knobValueAction;
	}
	
	background_ { |color|
		knobView.background_(color);
	}
	
	background {
		^knobView.background;
	}
	
	value {
		^knob.value;
	}
	stringColor_ { |color|
		label.stringColor_(color);
	}
	font_ { |fnt|
		label.font_(fnt);
	}
	knobColor_ { |colors|
		knob.color = colors;
	}
	spec_ { |sp|
		controlSpec = sp;
		this.value = prValue;
	}
	knobCentered_ { |flag|
		knob.centered = flag;
	}

}

+ JSCCompositeView {
	relativeOrigin_ { |val|
		postln("desn't do shit.");
	}
}

+ JSCNumberBox {
	scroll_step_ { |val|
		postln("whose fault is this anyway?");
	}
}
  