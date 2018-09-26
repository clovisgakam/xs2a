import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, UrlSegment } from '@angular/router';
import { BankingService } from '../../service/banking.service';
import { SinglePayments } from '../../models/models';
import { Banking } from '../../models/banking.model';
import { SinglePayment } from '../../models/singlePayment';
import { forEach } from '@angular-devkit/schematics';

@Component({
  selector: 'app-consent-confirmation-page',
  templateUrl: './consent-confirmation-page.component.html'
})
export class ConsentConfirmationPageComponent implements OnInit {
  singlePayments: SinglePayment;
  tan: string;
  paymentId: string;
  consentId: string;
  amount: number;

  constructor(private route: ActivatedRoute, private router: Router, private bankingService: BankingService) {
  }


  ngOnInit() {
    this.route.url
      .subscribe(params => {
        this.getBankingDetailsFromUrl(params);
      });

    let bankingData = <Banking>({tan: this.tan, consentId: this.consentId, paymentId: this.paymentId});
    this.bankingService.saveData(bankingData);
    this.getSinglePayments();
  }

  getSinglePayments(){
    this.bankingService.getSinglePayments().subscribe(data => {
      console.log('get', data);
      this.singlePayments = data;
    });
  }

  getBankingDetailsFromUrl(params: UrlSegment[]) {
    this.consentId = params[0].toString();
    this.paymentId = atob(params[1].toString());
  }

  createQueryParams() {
    return {
      consentId: this.consentId,
      paymentId: this.paymentId,
    };
  }

  onClickContinue() {
    this.bankingService.createPaymentConsent()
      .subscribe(data=>{
        console.log('post', data);
      });
    this.router.navigate(['/tanconfirmation'], {
      queryParams: this.createQueryParams()
    });
  }

  onClickCancel() {
    this.bankingService.setConsentStatus('REVOKED_BY_PSU')
      .subscribe();
    this.router.navigate(['/consentconfirmationdenied']);
  }
}
