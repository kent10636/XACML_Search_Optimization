
/*
 * @(#)FirstApplicablePolicyAlg.java
 *
 * Copyright 2003-2004 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   1. Redistribution of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 * 
 *   2. Redistribution in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Sun Microsystems, Inc. or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use in
 * the design, construction, operation or maintenance of any nuclear facility.
 */

package reqGen.com.sun.xacml.combine;



import java.net.URI;
import java.net.URISyntaxException;

import java.util.Iterator;
import java.util.List;

import reqGen.com.sun.xacml.AbstractPolicy;
import reqGen.com.sun.xacml.EvaluationCtx;
import reqGen.com.sun.xacml.MatchResult;
import reqGen.com.sun.xacml.ctx.Result;
import reqGen.com.sun.xacml.ctx.Status;


/**
 * This is the standard First Applicable policy combining algorithm. It looks
 * through the set of policies, finds the first one that applies, and returns
 * that evaluation result.
 *
 * @since 1.0
 * @author Seth Proctor
 */
public class FirstApplicablePolicyAlg extends PolicyCombiningAlgorithm
{
    
    /**
     * The standard URN used to identify this algorithm
     */
    public static final String algId =
        "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:" +
        "first-applicable";

    // a URI form of the identifier
    private static URI identifierURI;
    // exception if the URI was invalid, which should never be a problem
    private static RuntimeException earlyException;

    static {
        try {
            identifierURI = new URI(algId);
        } catch (URISyntaxException se) {
            earlyException = new IllegalArgumentException();
            earlyException.initCause(se);
        }
    }

    /**
     * Standard constructor.
     */
    public FirstApplicablePolicyAlg() {
        super(identifierURI);

        if (earlyException != null)
            throw earlyException;
    }

    /**
     * Applies the combining rule to the set of policies based on the
     * evaluation context.
     *
     * @param context the context from the request
     * @param policies the policies to combine
     *
     * @return the result of running the combining algorithm
     */
    public Result combine(EvaluationCtx context, List policies) {
        Iterator it = policies.iterator();
        
        System.out.println("FirstApplicablePolicyAlg combine");
        while (it.hasNext()) {
            AbstractPolicy policy = (AbstractPolicy)(it.next());

            // make sure that the policy matches the context
            MatchResult match = policy.match(context);

            if (match.getResult() == MatchResult.INDETERMINATE)
                return new Result(Result.DECISION_INDETERMINATE,
                                  match.getStatus(),
                                  context.getResourceId().encode());

            if (match.getResult() == MatchResult.MATCH) {
                // evaluate the policy
                Result result = policy.evaluate(context);
                int effect = result.getDecision();
                
                // in the case of PERMIT, DENY, or INDETERMINATE, we always
                // just return that result, so only on a rule that doesn't
                // apply do we keep going...
                if (effect != Result.DECISION_NOT_APPLICABLE)
                    return result;
            }
        }

        // if we got here, then none of the rules applied
        return new Result(Result.DECISION_NOT_APPLICABLE,
                          context.getResourceId().encode());
    }

}
