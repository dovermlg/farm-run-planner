package com.classictreerun;

/** Client-thread lifecycle of the selected setup's bank view. Does not open a bank or move items. */
final class SetupBankController
{
    static final String TAG="_farm_run_planner_setup_";
    interface View
    {
        default boolean available(){return true;}
        boolean bankOpen();
        String activeTag();
        void show(BankSetupRequest request);
        void removeOwnedView();
    }
    private final View view;
    private BankSetupRequest selected;
    SetupBankController(View view){this.view=view;}
    boolean ready(){return selected!=null && view.available();}
    void select(BankSetupRequest request)
    {
        if(request.mode==ClassicTreeRunConfig.SetupBankView.OFF || request.items.isEmpty()){clear();return;}
        boolean first=selected==null,changed=first || !selected.signature.equals(request.signature);
        selected=request;
        if(view.available() && view.bankOpen() && (request.focus || first || changed && TAG.equals(view.activeTag())))view.show(request);
    }
    void bankOpened(){if(selected!=null && view.available() && view.bankOpen())view.show(selected);}
    void bankClosed(){if(!view.bankOpen())suspend();}
    void availabilityChanged(){if(view.available())bankOpened();else suspend();}
    void suspend(){view.removeOwnedView();}
    void clear(){selected=null;view.removeOwnedView();}
}
