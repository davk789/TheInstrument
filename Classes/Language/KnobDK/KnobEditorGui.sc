
// blackrain at realizedsound dot net - 0106
//	03.10.2008 - Relative origin mods. Knob is a subclass of SCViewHolder now.

KnobEditorGui : EditorGui {
	var <>knob, <>numv, <>roundVal = 0.0001, size, <enabled=true, backColor, <>font;

	guiBody { arg layout, knobSize=32, numWidth=48, numHeight=14, hasBox=true, background;
		var cv, fontName, fontSize;
		layout.bounds = layout.bounds.width_(numWidth - 4);
		layout.bounds = layout.bounds.height_(knobSize + numHeight + 10);
		backColor = background ? Color.blue(0.1, 0.1);

		if (GUI.skins.notNil and:{ GUI.skins.crucial.notNil } ) {
			fontName = GUI.skins.crucial.fontSpecs[0];
			fontSize = GUI.skins.crucial.fontSpecs[1];
		}{
			fontName = "Helvetica";
			fontSize = 11;
		};
		font = GUI.font.new(fontName, fontSize);
		
		cv = GUI.compositeView.new(layout, layout.bounds)
			.relativeOrigin_(Knob.useRelativeOrigin);
		cv.decorator = FlowLayout.new(cv.bounds, 0@0, 0@4);

		this.kn(cv, knobSize);
		if(hasBox,{
			this.box(cv, numWidth-8, numHeight);
		});
	}
	
	kn { arg layout, size;
		knob = GUI.knob.new(layout, Rect(0,0,size,size));
		knob.color[0] = this.knobColor;
		knob.action_({arg v; 
			model.activeValue_(model.spec.map(v.value)).changed(this);
		});
		knob.receiveDragHandler = { arg kn;
			kn.valueAction = model.spec.unmap(SCView.currentDrag);
		};
		knob.beginDragAction = { model.value };
		knob.value = model.spec.unmap(model.poll);
		if(consumeKeyDowns,{ knob.keyDownAction = {nil}; });
	}

	box { arg layout, x, y=14;
		numv = GUI.numberBox.new(layout, Rect(0,0,x.max(40),y))
			.font_(font)
			.object_(model.poll)
			.action_({ arg nb;
				model.activeValue_(nb.value).changed(numv);
			})
			.canReceiveDragHandler_({ SCView.currentDrag.isFloat })
			.beginDragAction_({ model.value })
			.receiveDragHandler_({|v|
				var val = SCView.currentDrag;
				if (val < 0) {
					v.value = val.round(roundVal)
				}{
					v.value = val
				};
				model.activeValue_(SCView.currentDrag).changed(numv);
			});
		
		numv.value_(
			if (model.value < 0) {
				model.value.round(roundVal)
			}{
				model.value
			}
		);
		numv.step = roundVal;
		if(consumeKeyDowns,{ numv.keyDownAction = { nil }; });
	}
	
	centered_ { arg mode;
		knob.centered = mode;
	}
	
	enabled_ { arg state=true;
		enabled = state;
		[knob,numv].do(_.enabled_(enabled));
	}
	
	update {arg changed,changer;
		{
			var val;
			if(changer !== numv and: { numv.notNil }, {
				val = model.poll;
				if (val < 0) {
					val = val.round(roundVal);
				};
				numv.value_(val);
			});
			if(changer !== knob and: { knob.notNil }, {
					knob.value_(model.spec.unmap(model.poll));
			});
			nil
		}.defer;
	}

	background { ^backColor }
	knobColor { ^Color.blue(0.6, 0.3) }
}

KrKnobEditorGui : KnobEditorGui {
	knobColor { ^Color.blue(0.8, 0.4) }
}

IrKnobEditorGui : KnobEditorGui {
	knobColor { ^Color.black.alpha_(0.3) }
}
