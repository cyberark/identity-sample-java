import { Component, OnInit, ViewChild, ElementRef, SecurityContext } from '@angular/core';
import { UserService } from '../user/user.service';
import { Router } from '@angular/router';
import { HeaderComponent } from '../components/header/header.component';
import { FormGroup, FormControl, Validators, NgForm, FormBuilder } from '@angular/forms';
import { SafeResourceUrl, DomSanitizer } from '@angular/platform-browser';
declare var $: any;

@Component({
  selector: 'app-custom',
  templateUrl: './custom.component.html',
  styleUrls: ['./custom.component.css']
})
export class CustomComponent implements OnInit {
  @ViewChild('divToScroll') divToScroll: ElementRef;
  @ViewChild(HeaderComponent)
  private header: HeaderComponent;

  customForm: FormGroup;
  messageType = "error";
  errorMessage = "";
  loading = false;
  customData: any;
  showModal = false;
  selectedFile: File;
  imagePreview: SafeResourceUrl;

  constructor(
    private userService: UserService,
    private router: Router,
    private formBuilder: FormBuilder,
    private sanitizer: DomSanitizer
  ) { }

  ngOnInit() {
    if (localStorage.getItem("username") == null) {
      this.router.navigate(['/login']);
    }

    if (!localStorage.getItem("custom")) {
      this.router.navigate(['dashboard']);
    }

    let self = this;
    $(document).ready(function () {
      (<any>$('#accent-colorpalette')).colorPalettePicker({
        lines: 6,
        onSelected: function (color) {
          self.customForm.controls.accentColor.setValue(color);
          self.header.accentColor = color;
        }
      });
      (<any>$('#ribbon-colorpalette')).colorPalettePicker({
        lines: 6,
        onSelected: function (color) {
          self.customForm.controls.ribbonColor.setValue(color);
          self.header.ribbonColor = color;
        }
      });
    });

    this.customForm = this.formBuilder.group({
      "accentColor": ['', Validators.required],
      "ribbonColor": ['', Validators.required],
      "appImage": ['', Validators.required],
      "customerId": ['', Validators.required],
      "tenant": ['', Validators.required],
      "oauthAppId": ['', Validators.required],
      "oauthUser": ['', Validators.required],
      "oauthPassword": ['', Validators.required],
      "mfaRole": ['', Validators.required]
    }, { updateOn: 'blur' });

    this.loading = true;
    this.userService.getCustomData().subscribe(
      data => {
        this.loading = false;
        if (data.tenant) {
          this.customData = data;
          this.setCustomData();
        } else {
          this.setMessage("error", "Incorrect Data response");
        }
      }, error => {
        this.setMessage("error", "Error response");
      }
    );
  }

  onImageUpload(event) {
    this.selectedFile = event.target.files[0];
    const reader = new FileReader();
    reader.onload = () => {
      this.header.imageSource = this.imagePreview = reader.result.toString();
      this.customForm.controls.appImage.setValue(this.imagePreview);
    };
    reader.readAsDataURL(this.selectedFile);
  }

  setCustomData() {
    let customControls = this.customForm.controls;
    let custom = this.customData;
    customControls.accentColor.setValue(custom.accentColor);
    customControls.ribbonColor.setValue(custom.ribbonColor);
    let imageData = this.getTrimmedImageData(custom.appImage);
    customControls.appImage.setValue(imageData);
    this.imagePreview = this.sanitizer.bypassSecurityTrustResourceUrl(imageData || localStorage.getItem("logo")) || "../../../assets/images/logo.png";
    customControls.customerId.setValue(custom.customerId);
    customControls.tenant.setValue(custom.tenant);
    customControls.oauthAppId.setValue(custom.oauthAppId);
    customControls.oauthUser.setValue(custom.oauthUser);
    customControls.oauthPassword.setValue(custom.oauthPassword);
    customControls.mfaRole.setValue(custom.mfaRole);
  }

  getTrimmedImageData(appImage) {
    return appImage.substr(1, appImage.length - 2);
  }

  validateCustom(form: NgForm) {
    this.validateAllFormFields(this.customForm);
    if (this.customForm.invalid) {
      this.divToScroll.nativeElement.scrollTop = 0;
      return;
    }

    if (this.customData.customerId !== this.customForm.controls.customerId.value || this.customData.tenant !== this.customForm.controls.tenant.value) {
      return this.toggleCustomModal();
    } else {
      this.saveCustom(form);
    }
  }

  saveCustom(form: NgForm) {
    this.loading = true;
    this.userService.setCustomData(Object.assign({}, form)).subscribe(
      data => {
        this.loading = false;
        if (data.success == true) {
          localStorage.setItem("accent", this.customForm.controls.accentColor.value);
          localStorage.setItem("ribbon", this.customForm.controls.ribbonColor.value);
          localStorage.setItem("logo", this.sanitizer.sanitize(SecurityContext.RESOURCE_URL, this.imagePreview));
          this.userService.refreshActuators();
          if (this.customData.tenant !== this.customForm.controls.tenant.value) {
            localStorage.clear();
            this.router.navigate(['']);
          } else {
            this.setMessage("info", "Customization data updated successfully");
            this.router.navigate(['/custom']);
          }
        } else {
          this.setMessage("error", data.Message);
        }
      },
      error => {
        this.setMessage("error", error.message);
      }
    );
  }

  checkSelectedTab(href: string) {
    if (this.router.url == href) {
      return true;
    }
  }

  onClick(event) {
    if (event.target.attributes.id && event.target.attributes.id !== "" && event.target.attributes.id.nodeValue === "signOutButton") {
      return;
    }
    this.header.signOutMenu = false;
  }

  toggleCustomModal() {
    return this.showModal = !this.showModal;
  }

  checkMessageType() {
    return this.messageType == "info";
  }

  setMessage(messageType: string, message: string) {
    this.loading = false;
    this.messageType = messageType;
    this.errorMessage = message;
    this.divToScroll.nativeElement.scrollTop = 0;
  }

  validateAllFormFields(customForm: FormGroup): any {
    Object.keys(customForm.controls).forEach(field => {
      const control = customForm.get(field);
      if (control instanceof FormControl) {
        control.markAsTouched({ onlySelf: true });
      } else if (control instanceof FormGroup) {
        this.validateAllFormFields(control);
      }
    });
  }

  public hasError = (controlName: string, errorName: string) => {
    let form = this.customForm;
    let control = form.controls[controlName];
    return ((control.invalid && (control.dirty || control.touched)) && control.hasError(errorName));
  }
}