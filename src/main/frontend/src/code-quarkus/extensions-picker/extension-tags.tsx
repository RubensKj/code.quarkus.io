import React from 'react';

export function ExtensionTags(props: { status?: string }) {
    if (!props.status) {
        return <React.Fragment/>;
    }

    switch (props.status) {
        case 'preview':
            return (<span
                className="extension-tag preview"
                title="This is work in progress. API or configuration properties might change as the extension matures. Give us your feedback :)"
            >PREVIEW</span>);
        case 'experimental':
            return (<span
                className="extension-tag experimental"
                title="Early feedback is requested to mature the idea. There is no guarantee of stability nor long term presence in the platform until the solution matures."
            >EXPERIMENTAL</span>);
        case 'deprecated':
            return (<span
                title="This extension has been deprecated. It is likely to be replaced or removed in a future version of Quarkus."
                className="extension-tag deprecated"
            >DEPRECATED</span>);
        case 'provides-example':
            return (<span
                title="This extension provides example code to help you get started..."
                className="extension-tag example"
            ><span className="codestart-example-icon" /></span>);
        default:
            return <React.Fragment/>;
    }
}