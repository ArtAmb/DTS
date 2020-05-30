import { NgModule } from "@angular/core";
import { BrowserModule } from "@angular/platform-browser";
import { RouterModule } from "@angular/router";
import { ReactiveFormsModule, FormsModule } from "@angular/forms";
import { HttpClientModule, HTTP_INTERCEPTORS } from "@angular/common/http";
import { NgbModule } from "@ng-bootstrap/ng-bootstrap";

import { AppComponent } from "./app.component";
import { TopBarComponent } from "./top-bar/top-bar.component";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { ToastrModule } from "ngx-toastr";
import { LeftBarComponent } from "./left-bar/left-bar.component";
import { RightBarComponent } from "./right-bar/right-bar.component";
import { APP_ROUTES } from "./routes";
import { MainViewComponent } from "./main-view/main-view/main-view.component";

import { MatDialogModule } from "@angular/material/dialog";


@NgModule({
  imports: [
    BrowserModule,
    HttpClientModule,
    MatDialogModule,
    ReactiveFormsModule,
    NgbModule,
    FormsModule,
    BrowserAnimationsModule,
    ToastrModule.forRoot({
      timeOut: 1500
      // positionClass: 'toast-bottom-right'
    }),
    RouterModule.forRoot(APP_ROUTES)
  ],
  declarations: [
    AppComponent,
    TopBarComponent,
    LeftBarComponent,
    RightBarComponent,
    MainViewComponent,
  ],
  bootstrap: [AppComponent],
  providers: [],
  entryComponents: []
})
export class AppModule {}
// RouterModule.forRoot([{ path: '', component: LoginComponentComponent }])
