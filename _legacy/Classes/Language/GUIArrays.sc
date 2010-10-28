VSliderArray {
		*new { arg view, numSliders=1, xoff, yoff, width, height;
				var layout;
				layout = GUI.vLayoutView.new(view, Rect.new(xoff, yoff, width, height));
				^Array.fill(numSliders, {
						GUI.slider.new(layout, Rect.new(0, 0, width, (height / numSliders) - (((numSliders - 1) * 4) / numSliders) ));
						});
				}
}

HSliderArray {
	*new { arg view, numSliders= 1 , xoff, yoff, width, height;
		var layout;
		layout = GUI.hLayoutView.new(view, Rect.new(xoff, yoff, width, height));
		^Array.fill(numSliders, {
			GUI.slider.new(layout, Rect.new(0, 0, (width / numSliders) - (((numSliders - 1) * 4) / numSliders), height));
		});		
	}
}

/////////

VSliderArray2D {
	*new { arg view, numSliders=1, numArrays=1, xoff, yoff, width, height;
		var arrLayout;
		arrLayout = GUI.hLayoutView.new(view, Rect.new(xoff, yoff, (width * numArrays) + ((numArrays - 1) * 4), height));
		^Array.fill(numArrays, {
			var layout;
			layout = GUI.vLayoutView.new(arrLayout, Rect.new(0, 0, width, 0));
			Array.fill(numSliders, {
				GUI.slider.new(layout, Rect.new(0, 0, width, (height / numSliders) - (((numSliders - 1) * 4) / numSliders) ));
			});
		});
	}

}

/////////

VButtonArray {
		*new { arg view, numButtons=1, xoff, yoff, width, height;
				var layout;
				layout = GUI.vLayoutView.new(view, Rect.new(xoff, yoff, width, height));
				^Array.fill(numButtons, {
						GUI.button.new(layout, Rect.new(0, 0, width, (height / numButtons) - (((numButtons - 1) * 4) / numButtons) ));
						});
				}
}

HButtonArray {
		*new { arg view, numButtons= 1 , xoff, yoff, width, height;
				var layout;
				layout = GUI.hLayoutView.new(view, Rect.new(xoff, yoff, width, height));
				^Array.fill(numButtons, {
				GUI.button.new(layout, Rect.new(0, 0, (width / numButtons) - (((numButtons - 1) * 4) / numButtons), height));
				});
		
		}
}

VDragSinkArray {
		*new { arg view, numSinks=1, xoff, yoff, width, height;
				var layout;
				layout = GUI.vLayoutView.new(view, Rect.new(xoff, yoff, width, height));
				^Array.fill(numSinks, {
						GUI.dragSink.new(layout, Rect.new(0, 0, width, (height / numSinks) - (((numSinks - 1) * 4) / numSinks) ));
						});
				}
}

HDragSinkArray {
		*new { arg view, numSinks= 1 , xoff, yoff, width, height;
				var layout;
				layout = GUI.hLayoutView.new(view, Rect.new(xoff, yoff, width, height));
				^Array.fill(numSinks, {
				GUI.dragSink.new(layout, Rect.new(0, 0, (width / numSinks) - (((numSinks - 1) * 4) / numSinks), height));
				});
		
		}
}

VDragSourceArray {
		*new { arg view, numSources=1, xoff, yoff, width, height;
				var layout;
				layout = GUI.vLayoutView.new(view, Rect.new(xoff, yoff, width, height));
				^Array.fill(numSources, {
						GUI.dragSource.new(layout, Rect.new(0, 0, width, (height / numSources) - (((numSources - 1) * 4) / numSources) ));
						});
				}
}

HDragSourceArray {
		*new { arg view, numSources= 1 , xoff, yoff, width, height;
				var layout;
				layout = GUI.hLayoutView.new(view, Rect.new(xoff, yoff, width, height));
				^Array.fill(numSources, {
				GUI.dragSource.new(layout, Rect.new(0, 0, (width / numSources) - (((numSources - 1) * 4) / numSources), height));
				});
		
		}
}

VStaticTextArray {
		*new { arg view, numSources=1, xoff, yoff, width, height;
				var layout;
				layout = GUI.vLayoutView.new(view, Rect.new(xoff, yoff, width, height));
				^Array.fill(numSources, {
						GUI.staticText.new(layout, Rect.new(0, 0, width, (height / numSources) - (((numSources - 1) * 4) / numSources) ));
						});
				}
}

HStaticTextArray {
		*new { arg view, numSources= 1 , xoff, yoff, width, height;
				var layout;
				layout = GUI.hLayoutView.new(view, Rect.new(xoff, yoff, width, height));
				^Array.fill(numSources, {
					GUI.staticText.new(layout, Rect.new(0, 0, (width / numSources) - (((numSources - 1) * 4) / numSources), height));
					});
			
		}
}

VCompositeViewArray {
				classvar <>layout, <>arrays;
		*new { arg view, numViews=1, xoff, yoff, width, height;
				layout = GUI.vLayoutView.new(view, Rect.new(xoff, yoff, width, height));
				arrays = Array.fill(numViews, {
					GUI.compositeView.new(layout, Rect.new(0, 0, width, (height / numViews) - (((numViews - 1) * 4) / numViews) ));
					});
				}

		layout {
				^layout
				}
		arrays {
				^arrays
				}
		
}

HCompositeViewArray {
				classvar <>layout, <>arrays;
				
		*new { arg view, numViews= 1 , xoff, yoff, width, height;
				var layout;
				layout = GUI.hLayoutView.new(view, Rect.new(xoff, yoff, width, height));
				arrays = Array.fill(numViews, {
					GUI.compositeView.new(layout, Rect.new(0, 0, (width / numViews) - (((numViews - 1) * 4) / numViews), height));
					});
		
				}
		layout {
				^layout
				}
		arrays {
				^arrays
				}
}

