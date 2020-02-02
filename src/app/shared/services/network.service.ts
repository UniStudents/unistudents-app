import { Injectable } from '@angular/core';
import { Network } from '@ionic-native/network/ngx';

@Injectable({
  providedIn: 'root'
})
export class NetworkService {

  constructor(
      private network: Network
  ) { }

  isConnected(): boolean {
    return this.network.type !== 'none';
  }
}
