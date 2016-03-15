/*
 *     Copyright 2016 IBM Corp.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */


package com.jsonstore.dispatchers;

import android.app.Activity;
import android.content.Context;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

public class StoragePlugin extends DispatchingPlugin {

     public StoragePlugin () {
          // Initialize dispatchers.
          super();
     }

     @Override
     public void initialize (CordovaInterface cordova, CordovaWebView webView) {
          super.initialize (cordova, webView);
          
          Activity activity = this.cordova.getActivity();
          Context context = activity.getBaseContext();
          
          addDispatcher (new AllDirtyActionDispatcher(context));
          addDispatcher (new ChangePasswordActionDispatcher(context));
          addDispatcher (new CloseDatabaseActionDispatcher(context));
          addDispatcher (new DestroyDBFileAndKeychainActionDispatcher(context));
          addDispatcher (new DropTableActionDispatcher(context));
          addDispatcher (new CountActionDispatcher(context));
          addDispatcher (new FindActionDispatcher(context));
          addDispatcher (new AdvancedFindActionDispatcher(context));
          addDispatcher (new FindByIdActionDispatcher(context));
          addDispatcher (new IsDirtyActionDispatcher(context));
          addDispatcher (new IsKeyGenRequiredActionDispatcher(context));
          addDispatcher (new LocalCountActionDispatcher(context));
          addDispatcher (new MarkCleanActionDispatcher(context));
          addDispatcher (new ProvisionActionDispatcher(context));
          addDispatcher (new RemoveActionDispatcher(context));
          addDispatcher (new ReplaceActionDispatcher(context));
          addDispatcher (new StoreActionDispatcher(context));
          addDispatcher (new StoreDPKActionDispatcher(context));
          addDispatcher (new ClearActionDispatcher(context));
          addDispatcher (new ChangeActionDispatcher(context));
          addDispatcher (new FileInfoActionDispatcher(context));
          addDispatcher (new StartTransactionActionDispatcher(context));
          addDispatcher (new CommitTransactionActionDispatcher(context));
          addDispatcher (new RollbackTransactionActionDispatcher(context));
     }
}
