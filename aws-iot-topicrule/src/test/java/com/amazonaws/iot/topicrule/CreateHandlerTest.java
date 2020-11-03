package com.amazonaws.iot.topicrule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.iot.model.CreateTopicRuleRequest;
import software.amazon.awssdk.services.iot.model.GetTopicRuleRequest;
import software.amazon.awssdk.services.iot.model.GetTopicRuleResponse;
import software.amazon.awssdk.services.iot.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.iot.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.iot.model.ResourceAlreadyExistsException;
import software.amazon.awssdk.services.iot.model.Tag;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;

import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

    final CreateHandler handler = new CreateHandler();

    @Test
    public void handleRequest_SimpleSuccess() {
        when(iotClient.getTopicRule(any(GetTopicRuleRequest.class))).thenReturn(
                GetTopicRuleResponse.builder().rule(TOPIC_RULE).ruleArn(TOPIC_RULE_ARN).build());
        when(iotClient.listTagsForResource(any(ListTagsForResourceRequest.class)))
                .thenReturn(ListTagsForResourceResponse.builder().tags(DESIRED_RULE_TAGS.stream().map(tag -> Tag
                        .builder().key(tag.getKey()).value(tag.getValue()).build()).collect(Collectors.toList())).build());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, TEST_REQUEST, TEST_CALLBACK, proxyClient, LOGGER);
        assertThat(response, notNullValue());
        assertThat(response.getStatus(), equalTo(OperationStatus.SUCCESS));
        assertThat(response.getCallbackContext(), nullValue());
        assertThat(response.getCallbackDelaySeconds(), is(0));
        assertionOnResourceModels(response.getResourceModel(), TEST_REQUEST.getDesiredResourceState());
        assertThat(response.getResourceModels(), nullValue());
        assertThat(response.getMessage(), nullValue());
        assertThat(response.getErrorCode(), nullValue());
        verify(iotClient).getTopicRule(any(GetTopicRuleRequest.class));
    }

    @Test
    public void handleRequest_ResourceAlreadyExistsException() {
        when(iotClient.createTopicRule(any(CreateTopicRuleRequest.class))).thenThrow(ResourceAlreadyExistsException.builder().build());

        assertThrows(CfnAlreadyExistsException.class, () ->
                handler.handleRequest(proxy, TEST_REQUEST, TEST_CALLBACK, proxyClient, LOGGER));
        verify(iotClient, never()).getTopicRule(any(GetTopicRuleRequest.class));
    }
}