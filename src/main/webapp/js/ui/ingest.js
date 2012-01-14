Ext.onReady(function() {

	var cat_id = Page.queryString['catalog_id'];
	if (!cat_id)
		cat_id = "ERROR";

	var parentCatalog = Ext.ModelManager.getModel('Catalog').load(cat_id, {
		failure : function(record, operation) { //Due to Ext response handling stupidity, this actually won't be called
			new Ext.container.Viewport({
				layout : 'fit',
				items : [ new Ext.tab.Panel({
					title : "ERROR OCCURRED",
					layout : 'fit',
					region : 'center',
					margins : '2 2 2 2',
					border : false,
					defaults : {
						autoScroll : true
					},
					html : 'Something went wrong!'
				}) ]
			});
		},
		success : function(record, operation) {
			if (!record) {
				new Ext.container.Viewport({
					layout : 'fit',
					items : [ new Ext.tab.Panel({
						title : 'ERROR OCCURRED',
						layout : 'fit',
						region : 'center',
						margins : '2 2 2 2',
						border : false,
						defaults : {
							autoScroll : true
						},
						html : 'Invalid Catalog ID!'
					}) ]
				});

			} else {
				//################ NORMAL EXECUTION ##################
				var formItems = [];

//				var catalogForm = new ncETL.form.Model({
//					model : "Catalog",
//					defaults : {
//						anchor : '100%'
//					}
//				});
//				catalogForm.loadRecord(record);
//				formItems.push(catalogForm);

				var ingestStore = record.ingestors();

				var ingestGrid = new ncETL.grid.Ingestor({
					store : ingestStore
				});
				
//				ingestStore.each(function(item) {
//					var inges = new ncETL.form.Model({
//						model : 'Ingestor',
//						defaults : {
//							anchor : '100%'
//						}
//					});
//					inges.loadRecord(item);
//					formItems.push(inges);
//				});

				var editMetadataPanel = new ncETL.panel.ModelForm({
					title : 'Edit Metadata',
					model : record,
					border : false,
					padding : '2 2 2 2'
				});

				var ncISOPanel = new ncETL.panel.ncISO({
					title : 'ncISO',
					layout : 'fit',
					border : false,
                                        filename : record.get('location')
					//contentEl : 'decorate'
				});

				new Ext.container.Viewport({
					layout : 'fit',
					items : [ new Ext.tab.Panel({
						title : record.get('name'),
						layout : 'fit',
						region : 'center',
						margins : '2 2 2 2',
						border : false,
						defaults : {
							autoScroll : true
						},
						items : [ ingestGrid, editMetadataPanel, ncISOPanel ]
					}) ]
				});
			}
		}
	});

});