import { Controller, Get, Header } from '@nestjs/common';
import { ApiOperation, ApiTags } from '@nestjs/swagger';
import { AppService } from './app.service';

@ApiTags('app')
@Controller()
export class AppController {
  constructor(private readonly appService: AppService) {}

  @ApiOperation({ summary: 'Service metadata / hello' })
  @Get()
  async getHello() {
    return this.appService.getHello();
  }

  @ApiOperation({ summary: 'Public account-deletion request page' })
  @Header('Content-Type', 'text/html; charset=utf-8')
  @Get('account-deletion')
  accountDeletionPage() {
    return `<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8" />
  <meta name="viewport" content="width=device-width,initial-scale=1" />
  <title>Delete ReadRealm Account</title>
  <style>
    :root { color-scheme: dark; font-family: system-ui, -apple-system, sans-serif; }
    body { margin: 0; background: #1A0F0A; color: #F5E6C8; }
    main { max-width: 620px; margin: 0 auto; padding: 48px 20px; }
    .card { background: #2b1913; border: 1px solid #8B5A2B; border-radius: 18px; padding: 24px; }
    h1 { color: #D4AF37; margin-top: 0; }
    label { display:block; margin:16px 0 6px; }
    input { box-sizing:border-box; width:100%; padding:12px; border-radius:10px; border:1px solid #8B5A2B; background:#1A0F0A; color:#F5E6C8; }
    button { margin-top:16px; padding:12px 16px; border:0; border-radius:10px; font-weight:700; cursor:pointer; }
    .primary { background:#D4AF37; color:#1A0F0A; }
    .danger { background:#8B0000; color:white; }
    .hidden { display:none; }
    #status { margin-top:16px; min-height:24px; }
  </style>
</head>
<body>
<main>
  <div class="card">
    <h1>Delete your ReadRealm account</h1>
    <p>Enter the email address for your ReadRealm account. We will send a verification code before permanently deleting the account and associated ReadRealm data.</p>
    <p>This includes your account, active sessions, reviews, bookmarks, and chat messages. Deletion cannot be undone.</p>

    <section id="requestStep">
      <label for="email">Email address</label>
      <input id="email" type="email" autocomplete="email" required />
      <button class="primary" id="requestButton">Send deletion code</button>
    </section>

    <section id="confirmStep" class="hidden">
      <label for="otp">Verification code</label>
      <input id="otp" inputmode="numeric" autocomplete="one-time-code" required />
      <button class="danger" id="confirmButton">Delete account permanently</button>
    </section>
    <div id="status" role="status" aria-live="polite"></div>
  </div>
</main>
<script>
  const email = document.getElementById('email');
  const otp = document.getElementById('otp');
  const status = document.getElementById('status');
  const confirmStep = document.getElementById('confirmStep');

  document.getElementById('requestButton').addEventListener('click', async () => {
    status.textContent = 'Sending code…';
    const response = await fetch('/auth/account-deletion/request', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({email: email.value})
    });
    const body = await response.json().catch(() => ({}));
    status.textContent = body.message || (response.ok ? 'Check your email for the deletion code.' : 'Unable to send a code.');
    if (response.ok) confirmStep.classList.remove('hidden');
  });

  document.getElementById('confirmButton').addEventListener('click', async () => {
    if (!confirm('Permanently delete this ReadRealm account and associated data?')) return;
    status.textContent = 'Deleting account…';
    const response = await fetch('/auth/account-deletion/confirm', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({email: email.value, otp: otp.value})
    });
    const body = await response.json().catch(() => ({}));
    status.textContent = body.message || (response.ok ? 'Your ReadRealm account has been deleted.' : 'Deletion failed. Check the code and try again.');
    if (response.ok) {
      document.getElementById('requestStep').classList.add('hidden');
      confirmStep.classList.add('hidden');
    }
  });
</script>
</body>
</html>`;
  }

  @ApiOperation({ summary: 'Service health check' })
  @Get('health')
  health() {
    return {
      status: 'ok',
      service: 'readrealm-api',
      timestamp: new Date().toISOString(),
    };
  }

  @ApiOperation({ summary: 'Service liveness check' })
  @Get('health/live')
  live() {
    return { status: 'ok' };
  }
}
