import { Injectable, ServiceUnavailableException } from '@nestjs/common';
import { HttpService } from '@nestjs/axios';
import { ConfigService } from '@nestjs/config';
import { firstValueFrom } from 'rxjs';

@Injectable()
export class SpeechRealtimeService {
  private readonly signedUrlEndpoint =
    'https://api.elevenlabs.io/v1/convai/conversation/get-signed-url';

  constructor(
    private readonly httpService: HttpService,
    private readonly configService: ConfigService,
  ) {}

  async getSignedUrl(): Promise<string> {
    const apiKey = this.configService.get<string>('elevenlabs.apiKey');
    const agentId = this.configService.get<string>('elevenlabs.agentId');

    if (!apiKey || !agentId) {
      throw new ServiceUnavailableException(
        'ElevenLabs Agents is not configured. Set ELEVENLABS_API_KEY and ELEVENLABS_AGENT_ID.',
      );
    }

    const response = await firstValueFrom(
      this.httpService.get(this.signedUrlEndpoint, {
        headers: {
          'xi-api-key': apiKey,
        },
        params: {
          agent_id: agentId,
          include_conversation_id: true,
        },
      }),
    );

    const signedUrl = response.data?.signed_url;
    if (!signedUrl) {
      throw new ServiceUnavailableException(
        'ElevenLabs did not return a signed conversation URL.',
      );
    }

    return signedUrl;
  }
}
