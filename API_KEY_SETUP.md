# OpenAI API Key Configuration Guide

## 1. Get OpenAI API Key

1. Visit [OpenAI Platform](https://platform.openai.com/api-keys)
2. Log in to your OpenAI account
3. Click "Create new secret key"
4. Copy the generated API Key (starts with `sk-`)

## 2. Configure API Key

### Method 1: Modify Configuration File
Edit the `src/main/resources/application.properties` file:

```properties
# Replace your-openai-api-key-here below with your real API Key
openai.api.key=sk-your-actual-api-key-here
```

### Method 2: Environment Variables (Recommended for Production)
```bash
export OPENAI_API_KEY=sk-your-actual-api-key-here
```

## 3. Test if API Key is Valid

### Method 1: Using Browser
1. After starting the application, visit: `http://localhost:8080/api/recommendations/test-openai`
2. If you see detailed restaurant recommendations, the API Key is configured successfully
3. If you see error messages, please check if the API Key is correct

### Method 2: Using curl Command
```bash
curl http://localhost:8080/api/recommendations/test-openai
```

### Method 3: Using Test Script
```bash
./test_ai.sh
```

### Method 4: Using Postman
- URL: `GET http://localhost:8080/api/recommendations/test-openai`
- Send request to view response

## 4. Common Issues

### Issue 1: Invalid API Key
**Error Message**: `OpenAI API key is invalid or expired`
**Solution**:
- Check if the API Key is copied correctly
- Confirm if the API Key is valid (check on OpenAI platform)
- Check if account balance is sufficient

### Issue 2: API Call Frequency Too High
**Error Message**: `API call frequency is too high. Please try again later`
**Solution**:
- Wait for a while and try again
- Check rate limits on OpenAI account

### Issue 3: Model Deprecated
**Error Message**: `The model text-davinci-003 has been deprecated`
**Solution**:
- ✅ Fixed: Now using `gpt-3.5-turbo` model
- Restart the application to resolve

### Issue 4: Quota Exceeded
**Error Message**: `You exceeded your current quota, please check your plan and billing details`
**Solution**:
- Check OpenAI account balance
- Add payment method and recharge account
- Set usage limits

### Issue 5: Network Connection Problem
**Error Message**: `Sorry, we are temporarily unable to recommend restaurants`
**Solution**:
- Check network connection
- Confirm firewall settings
- Try using proxy if needed

## 5. Prompt Engineering Location

Prompt Engineering code is located at:
- File: `src/main/java/com/restaurant/recommendation/service/OpenAIService.java`
- Method: `buildPrompt(RecommendationRequest request)`

### Currently Used Model
- **Model**: `gpt-3.5-turbo`
- **API Type**: ChatGPT API
- **Max Tokens**: 800
- **Temperature**: 0.7

### Current Prompt Structure
1. **Role Definition**: Restaurant recommendation expert
2. **User Requirements**: Preference, location, cuisine, price, number of people, occasion
3. **Recommendation Requirements**: Formatted output requirements
4. **Language Requirements**: English answers

## 6. Complete Testing Process

### Start Application
```bash
# Start with IDE, or
./run.sh
```

### Test All Functions
```bash
./test_ai.sh
```

### Manual Testing
1. **Health Check**: `GET http://localhost:8080/api/recommendations/health`
2. **AI Test**: `GET http://localhost:8080/api/recommendations/test-openai`
3. **Recommendation Endpoint**: `POST http://localhost:8080/api/recommendations`

## 7. Security Considerations

⚠️ **Important**:
- Do not commit API Key to version control system
- Use environment variables in production environment
- Regularly rotate API Key
- Monitor API usage and costs

## 8. Cost Information

- OpenAI API charges by usage
- GPT-3.5-turbo is cheaper than text-davinci-003
- Recommend setting usage limits and monitoring
- Can view usage on OpenAI platform

## 9. Update Log

### v2.0 (Latest)
- ✅ Fixed model deprecation issue
- ✅ Upgraded to gpt-3.5-turbo model
- ✅ Use ChatGPT API instead of Completion API
- ✅ Added complete test scripts
- ✅ Improved error handling

### v1.0
- Initial version
- Used text-davinci-003 model 