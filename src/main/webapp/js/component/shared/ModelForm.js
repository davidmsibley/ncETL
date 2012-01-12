Ext.define("ncETL.form.Model", {
    extend: 'Ext.form.Panel',
    saveRecord: function() {
        var me = this;
		
        var rec = me.getRecord();
        var form = me.getForm();
		
        form.updateRecord(rec);
		
        if (rec.dirty) {
            rec.save();
        }
		
    },
    constructor : function(config) {
        if (!config) config = {};
		
        //var _modelName = config.model;
        //var _model = Ext.ModelManager.getModel(_modelName).create();
	var _model = config.model;
	
        var _items = [];
		
        _model.fields.each(function(item) {
            var _editor = item.editor;
            var _itemName = item.name;
            if (_editor) {
                _items.push(_editor);
            } else if (_itemName){
                _items.push({
                    xtype: 'displayfield',
                    fieldLabel: _itemName,
                    name : _itemName
                });
            }
        }, this);
        
        config = Ext.apply({
            title : _model.modelName,
            items : _items
        }, config);
        ncETL.form.Model.superclass.constructor.call(this, config);
    }
});